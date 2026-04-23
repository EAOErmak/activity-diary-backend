package com.example.activity_diary.service.impl.auth;

import com.example.activity_diary.dto.auth.AuthResponseDto;
import com.example.activity_diary.dto.auth.AuthRequestDto;
import com.example.activity_diary.entity.RefreshToken;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.exception.types.ForbiddenException;
import com.example.activity_diary.exception.types.UnauthorizedException;
import com.example.activity_diary.platform.web.auth.service.LoginEventService;
import com.example.activity_diary.platform.web.auth.service.RefreshTokenService;
import com.example.activity_diary.platform.web.auth.service.impl.AuthServiceImpl;
import com.example.activity_diary.platform.web.security.JwtUtils;
import com.example.activity_diary.repository.RegistrationEventRepository;
import com.example.activity_diary.repository.UserAccountRepository;
import com.example.activity_diary.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private LoginEventService loginEventService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RegistrationEventRepository registrationEventRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private AuthServiceImpl service;

    @Test
    void login_whenAccountMissing_performsDummyPasswordCheckAndThrowsUnauthorized() {
        AuthRequestDto request = new AuthRequestDto();
        request.setEmail("missing@example.com");
        request.setPassword("secret");

        when(userAccountRepository.findByProviderAndProviderId(
                com.example.activity_diary.entity.enums.ProviderType.LOCAL,
                "missing@example.com"
        )).thenReturn(Optional.empty());

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.login(request, null)
        );

        assertEquals("Invalid email or password", ex.getMessage());
        verify(passwordEncoder).matches(eq("secret"), anyString());
    }

    @Test
    void refresh_rotatesTokensAndReturnsNewAuthPayload() {
        User user = User.builder()
                .username("alice")
                .enabled(true)
                .role(Role.USER)
                .build();
        user.setId(7L);

        RefreshToken stored = RefreshToken.builder()
                .user(user)
                .tokenHash("old-hash")
                .build();

        when(refreshTokenService.verify("old-refresh")).thenReturn(stored);
        when(jwtUtils.generateAccessToken(7L, "alice", "USER")).thenReturn("new-access");
        when(jwtUtils.generateRefreshToken(7L, "alice", "USER")).thenReturn("new-refresh");

        AuthResponseDto result = service.refresh("old-refresh");

        assertEquals("new-access", result.getAccessToken());
        assertEquals("new-refresh", result.getRefreshToken());
        assertEquals("alice", result.getUsername());
        assertEquals(7L, result.getUserId());
        assertEquals("USER", result.getRole());
        verify(refreshTokenService).rotate(stored, "new-refresh");
    }

    @Test
    void refresh_whenUserDisabled_throwsForbidden() {
        User user = User.builder()
                .username("alice")
                .enabled(false)
                .role(Role.USER)
                .build();
        RefreshToken stored = RefreshToken.builder().user(user).build();
        when(refreshTokenService.verify("refresh")).thenReturn(stored);

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> service.refresh("refresh")
        );

        assertEquals("Account is disabled", ex.getMessage());
        verify(refreshTokenService, never()).rotate(eq(stored), anyString());
    }

    @Test
    void refresh_whenUserLocked_throwsForbidden() {
        User user = User.builder()
                .username("alice")
                .enabled(true)
                .role(Role.USER)
                .build();
        user.lockUntil(LocalDateTime.now().plusMinutes(10));

        RefreshToken stored = RefreshToken.builder().user(user).build();
        when(refreshTokenService.verify("refresh")).thenReturn(stored);

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> service.refresh("refresh")
        );

        assertEquals(true, ex.getMessage().startsWith("Account locked until "));
        verify(refreshTokenService, never()).rotate(eq(stored), anyString());
    }
}
