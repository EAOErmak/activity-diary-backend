package com.example.activity_diary.service.impl.auth;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.RefreshToken;
import com.example.activity_diary.exception.types.UnauthorizedException;
import com.example.activity_diary.repository.RefreshTokenRepository;
import com.example.activity_diary.service.auth.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(token.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Token hashing failed", e);
        }
    }

    @Override
    public RefreshToken create(User user) {
        String rawToken = java.util.UUID.randomUUID().toString();

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(Instant.now().plusSeconds(60L * 60 * 24 * 30)) // 30 days
                .build();

        repository.save(token);

        token.setTokenHash(rawToken); // возвращаем в чистом виде ВРЕМЕННО
        return token;
    }

    @Override
    public RefreshToken verifyAndRotate(String rawToken) {
        String hashed = hash(rawToken);

        RefreshToken rt = repository.findByTokenHash(hashed)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (rt.isRevoked() || rt.getExpiresAt().isBefore(Instant.now()))
            throw new UnauthorizedException("Expired or revoked refresh token");

        rt.setRevoked(true);
        repository.save(rt);

        RefreshToken newRt = create(rt.getUser());
        rt.setReplacedBy(newRt.getId());
        repository.save(rt);

        return newRt;
    }

    @Override
    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        repository.save(token);
    }
}
