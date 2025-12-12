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

    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private Key signingKey;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes (256 bits)"
            );
        }
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // === GENERATION ===
    // NOTE: use the new overloads where you have access to user id and role.
    // Example usage on login:
    // String access = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getRole().name());
    // String refresh = jwtUtils.generateRefreshToken(user.getId(), user.getUsername(), user.getRole().name());

    public String generateAccessToken(Long userId, String username, String role) {
        return generateToken(userId, username, role, "access", accessExpirationMs);
    }

    public String generateRefreshToken(Long userId, String username, String role) {
        return generateToken(userId, username, role, "refresh", refreshExpirationMs);
    }

    private String generateToken(Long userId, String username, String role, String type, long ttlMs) {
        long now = System.currentTimeMillis();
        JwtBuilder b = Jwts.builder()
                .setSubject(username)
                .claim("type", type)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMs))
                .signWith(signingKey, SignatureAlgorithm.HS256);

        if (userId != null) {
            b.claim("id", userId);
        }
        if (role != null) {
            b.claim("role", role);
        }
        return b.compact();
    }

    // === EXTRACTION ===
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractTokenType(String token) {
        Object type = extractAllClaims(token).get("type");
        return type == null ? null : type.toString();
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object idObj = claims.get("id");
        if (idObj == null) return null;
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }
        try {
            return Long.parseLong(idObj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        Object r = claims.get("role");
        return r == null ? null : r.toString();
    }

    // === VALIDATION ===
    public boolean isAccessTokenValid(String token) {
        return isTokenValid(token, "access");
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token, "refresh");
    }

    private boolean isTokenValid(String token, String expectedType) {
        try {
            Claims claims = extractAllClaims(token);
            String actualType = claims.get("type", String.class);
            if (expectedType != null && actualType != null && !expectedType.equals(actualType)) {
                return false;
            }
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // === INTERNAL ===
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
