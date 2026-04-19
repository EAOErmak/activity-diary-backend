package com.example.activity_diary.service.impl.auth;

import com.example.activity_diary.entity.RefreshToken;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.exception.types.ForbiddenException;
import com.example.activity_diary.platform.web.auth.service.impl.RefreshTokenServiceImpl;
import com.example.activity_diary.platform.web.security.JwtUtils;
import com.example.activity_diary.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository repository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private RefreshTokenServiceImpl service;

    @Test
    void save_storesHashedTokenWithJwtExpiration() throws Exception {
        User user = User.builder().username("user").build();
        String rawToken = "refresh-token";
        Instant expiresAt = Instant.parse("2026-05-01T00:00:00Z");
        when(jwtUtils.extractExpiration(rawToken)).thenReturn(Date.from(expiresAt));

        service.save(user, rawToken);

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(tokenCaptor.capture());

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String expectedHash = Base64.getEncoder().encodeToString(
                digest.digest(rawToken.getBytes(StandardCharsets.UTF_8))
        );

        RefreshToken saved = tokenCaptor.getValue();
        assertSame(user, saved.getUser());
        assertEquals(expectedHash, saved.getTokenHash());
        assertEquals(expiresAt, saved.getExpiresAt());
    }

    @Test
    void verify_whenJwtInvalid_throwsForbiddenWithoutRepositoryLookup() {
        when(jwtUtils.isRefreshTokenValid("bad-token")).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> service.verify("bad-token"));

        verify(repository, never()).findActiveByTokenHash(any(), any());
    }

    @Test
    void verify_whenJwtValidAndTokenStored_returnsStoredToken() {
        String rawToken = "good-token";
        String expectedHash = digestToBase64(rawToken);
        RefreshToken stored = RefreshToken.builder().tokenHash(expectedHash).build();

        when(jwtUtils.isRefreshTokenValid(rawToken)).thenReturn(true);
        when(repository.findActiveByTokenHash(eq(expectedHash), any(Instant.class)))
                .thenReturn(Optional.of(stored));

        RefreshToken result = service.verify(rawToken);

        assertSame(stored, result);
    }

    private static String digestToBase64(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(
                    digest.digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
