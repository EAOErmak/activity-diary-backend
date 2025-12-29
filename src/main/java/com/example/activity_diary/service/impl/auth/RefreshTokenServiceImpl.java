package com.example.activity_diary.service.impl.auth;

import com.example.activity_diary.entity.RefreshToken;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.exception.types.ForbiddenException;
import com.example.activity_diary.repository.RefreshTokenRepository;
import com.example.activity_diary.service.auth.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Override
    public void save(User user, String rawToken) {

        String hashed = hash(rawToken);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(hashed)
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();

        repository.save(token);
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken verify(String rawToken) {

        String hashed = hash(rawToken);

        RefreshToken token = repository
                .findActiveByTokenHash(hashed, Instant.now())
                .orElseThrow(() ->
                        new ForbiddenException("Invalid or expired refresh token")
                );

        return token;
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

    private String generateRawToken() {
        return UUID.randomUUID().toString();
    }
}
