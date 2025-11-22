package com.example.activity_diary.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
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

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ============================================================
    // TOKEN GENERATION
    // ============================================================

    public String generateAccessToken(String username) {
        return buildToken(username, accessExpirationMs);
    }

    public String generateRefreshToken(String username) {
        return buildToken(username, refreshExpirationMs);
    }

    private String buildToken(String username, long expirationTime) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ============================================================
    // EXTRACTION
    // ============================================================

    public String extractUsername(String token) {
        return validateAndParse(token).getBody().getSubject();
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    public boolean isValid(String token) {
        try {
            validateAndParse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isExpired(String token) {
        try {
            Date exp = validateAndParse(token).getBody().getExpiration();
            return exp.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    private Jws<Claims> validateAndParse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
    }
}
