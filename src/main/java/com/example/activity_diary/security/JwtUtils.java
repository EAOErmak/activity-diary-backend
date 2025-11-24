package com.example.activity_diary.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long accessExpirationMs;

    private Key signingKey;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes (256 bits).");
        }
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ============================================================
    // GENERATION
    // ============================================================

    public String generateAccessToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // ============================================================
    // EXTRACTION
    // ============================================================

    public String extractUsername(String accessToken) {
        return parse(accessToken).getBody().getSubject();
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    public boolean isAccessTokenValid(String accessToken) {
        try {
            parse(accessToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAccessTokenExpired(String accessToken) {
        try {
            return parse(accessToken).getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // ============================================================
    // INTERNAL
    // ============================================================

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
    }
}
