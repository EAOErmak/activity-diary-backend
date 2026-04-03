package com.example.activity_diary.service.impl.auth;

import com.example.activity_diary.entity.RefreshToken;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.exception.types.ForbiddenException;
import com.example.activity_diary.repository.RefreshTokenRepository;
import com.example.activity_diary.security.JwtUtils;
import com.example.activity_diary.service.auth.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtUtils jwtUtils;

    @Override
    public void save(User user, String rawToken) {

        String hashed = hash(rawToken);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(hashed)
                .expiresAt(jwtUtils.extractExpiration(rawToken).toInstant())
                .build();

        repository.save(token);
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken verify(String rawToken) {
        if (!jwtUtils.isRefreshTokenValid(rawToken)) {
            throw new ForbiddenException("Invalid or expired refresh token");
        }

        String hashed = hash(rawToken);

        return repository
                .findActiveByTokenHash(hashed, Instant.now())
                .orElseThrow(() ->
                        new ForbiddenException("Invalid or expired refresh token")
                );
    }

    @Override
    public void revoke(RefreshToken token) {
        token.revoke();
        repository.save(token);
    }

    @Override
    public void revokeByToken(String rawToken) {

        String hashed = hash(rawToken);

        repository.findActiveByTokenHash(hashed, Instant.now())
                .ifPresent(token -> {
                    token.revoke();
                    repository.save(token);
                });
    }

    @Override
    public void revokeAllByUser(User user) {
        repository.revokeAllByUser(user);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("Refresh token hashing failed", e);
        }
    }
}
