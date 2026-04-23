package com.example.activity_diary.platform.web.auth.service.impl;

import com.example.activity_diary.entity.RefreshToken;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.exception.types.ForbiddenException;
import com.example.activity_diary.repository.RefreshTokenRepository;
import com.example.activity_diary.platform.web.security.JwtUtils;
import com.example.activity_diary.platform.web.auth.service.RefreshTokenService;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service

@RequiredArgsConstructor
@Transactional
@Slf4j
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
    public RefreshToken rotate(RefreshToken token, String rawReplacementToken) {
        if (!token.isActive()) {
            throw new ForbiddenException("Invalid or expired refresh token");
        }

        try {
            RefreshToken replacement = RefreshToken.builder()
                    .user(token.getUser())
                    .tokenHash(hash(rawReplacementToken))
                    .expiresAt(jwtUtils.extractExpiration(rawReplacementToken).toInstant())
                    .build();

            repository.save(replacement);
            token.replaceWith(replacement);
            repository.save(token);
            repository.flush();
            return replacement;
        } catch (ObjectOptimisticLockingFailureException
                 | OptimisticLockException
                 | PessimisticLockingFailureException e) {
            throw new ForbiddenException("Refresh token has already been used");
        }
    }

    @Override
    public void revoke(RefreshToken token) {
        token.revoke();
        repository.save(token);
        repository.flush();
    }

    @Override
    public void revokeByToken(String rawToken) {

        String hashed = hash(rawToken);

        repository.findActiveByTokenHash(hashed, Instant.now())
                .ifPresent(token -> {
                    token.revoke();
                    try {
                        repository.save(token);
                        repository.flush();
                    } catch (ObjectOptimisticLockingFailureException
                             | OptimisticLockException
                             | PessimisticLockingFailureException e) {
                        log.debug("Refresh token already rotated or revoked during logout");
                    }
                });
    }

    @Override
    public void revokeAllByUser(User user) {
        repository.revokeAllByUser(user);
    }

    @Scheduled(cron = "${auth.refresh.cleanup-cron:0 0 * * * *}")
    public void deleteExpiredTokens() {
        repository.deleteAllExpired(Instant.now());
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
