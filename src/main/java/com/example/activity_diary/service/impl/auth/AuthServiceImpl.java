package com.example.activity_diary.service.impl.auth;

import com.example.activity_diary.dto.auth.AuthRequestDto;
import com.example.activity_diary.dto.auth.AuthResponseDto;
import com.example.activity_diary.dto.auth.RegisterRequestDto;
import com.example.activity_diary.entity.log.RegistrationEvent;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.RefreshToken;
import com.example.activity_diary.repository.RegistrationEventRepository;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.ForbiddenException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.security.JwtUtils;
import com.example.activity_diary.service.auth.AuthService;
import com.example.activity_diary.service.auth.RefreshTokenService;
import com.example.activity_diary.service.auth.VerificationService;
import com.example.activity_diary.service.login.LoginEventService;
import com.example.activity_diary.service.sync.UserSyncService;
import com.example.activity_diary.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final RefreshTokenService refreshTokenService;
    private final UserSyncService userSyncService;
    private final LoginEventService loginEventService;
    private final JwtUtils jwtUtils;
    private final RegistrationEventRepository registrationEventRepository;

    private static final int MAX_ACCOUNTS_PER_IP = 10;

    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoO5uG08CqslfQJz8l8bY5v3y5i7x3UXZG";

    // ============================================================
    // REGISTER
    // ============================================================

    @Override
    public AuthResponseDto register(RegisterRequestDto req, HttpServletRequest request) {

        String ip = IpUtils.getClientIp(request);
        String username = req.getUsername().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            fakeDelay();
            throw new BadRequestException("Invalid username or password");
        }

        long accountsFromIp = registrationEventRepository.countByIp(ip);
        if (accountsFromIp >= MAX_ACCOUNTS_PER_IP) {
            fakeDelay();
            throw new ForbiddenException("Too many accounts created from your IP");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName().trim())
                .enabled(false)
                .build();

        userRepository.save(user);
        userSyncService.initUser(user.getId());

        registrationEventRepository.save(
                RegistrationEvent.builder()
                        .user(user)
                        .ip(ip)
                        .build()
        );

        return AuthResponseDto.builder()
                .username(user.getUsername())
                .userId(user.getId())
                .role(user.getRole().name())
                .twoFactorRequired(true)
                .build();
    }

    // ============================================================
    // VERIFICATION (ACCOUNT)
    // ============================================================

    @Override
    public void requestVerificationCode(String username) {

        String normalized = username.trim().toLowerCase();
        User user = userRepository.findByUsername(normalized).orElse(null);

        // Всегда одинаковое поведение
        if (user == null || !user.isEnabled()) {
            fakeDelay();
            return;
        }

        verificationService.createAndSendCode(user.getUsername(), user.getChatId());
    }

    @Override
    public void verifyCode(String username, String code) {

        boolean ok = verificationService.verify(username.trim().toLowerCase(), code);
        if (!ok) {
            throw new BadRequestException("Invalid or expired verification code");
        }

        User user = userRepository.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getChatId() == null) {
            throw new ForbiddenException("Telegram verification not completed");
        }

        user.enable();
        userRepository.save(user);
    }

    // ============================================================
    // LOGIN
    // ============================================================

    @Override
    public AuthResponseDto login(AuthRequestDto req, HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String username = req.getUsername().trim().toLowerCase();

        User user = userRepository.findByUsername(username).orElse(null);

        boolean passwordMatches;

        if (user != null) {
            passwordMatches = passwordEncoder.matches(req.getPassword(), user.getPassword());
        } else {
            passwordEncoder.matches(req.getPassword(), DUMMY_PASSWORD_HASH);
            fakeDelay();
            loginEventService.recordFailure(ip, userAgent);
            throw new BadRequestException("Invalid username or password");
        }

        if (!passwordMatches || !user.isEnabled()) {
            fakeDelay();
            loginEventService.recordFailure(ip, userAgent);
            throw new BadRequestException("Invalid username or password");
        }

        checkForLock(user);

        loginEventService.recordSuccess(user.getId(), ip, userAgent);

        verificationService.createAndSendLoginCode(
                user.getUsername(),
                user.getChatId()
        );

        return AuthResponseDto.builder()
                .twoFactorRequired(true)
                .username(user.getUsername())
                .userId(user.getId())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponseDto confirmLogin(String username, String code) {

        boolean ok = verificationService.verifyLoginCode(username, code);
        if (!ok) {
            throw new BadRequestException("Invalid or expired 2FA code");
        }

        User user = userRepository.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new ForbiddenException("User not verified");
        }

        checkForLock(user);

        String accessToken = jwtUtils.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

        refreshTokenService.save(user, refreshToken);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .userId(user.getId())
                .role(user.getRole().name())
                .build();
    }

    // ============================================================
    // REFRESH (ROTATION)
    // ============================================================

    @Override
    public AuthResponseDto refresh(String rawRefreshToken) {

        RefreshToken stored = refreshTokenService.verify(rawRefreshToken);

        User user = stored.getUser();

        if (!user.isEnabled()) {
            throw new ForbiddenException("User not verified");
        }

        checkForLock(user);

        refreshTokenService.revoke(stored);

        String newAccess = jwtUtils.generateAccessToken(user.getUsername());
        String newRefresh = jwtUtils.generateRefreshToken(user.getUsername());

        refreshTokenService.save(user, newRefresh);

        return AuthResponseDto.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .username(user.getUsername())
                .userId(user.getId())
                .role(user.getRole().name())
                .build();
    }

    // ============================================================
    // LOGOUT
    // ============================================================

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeByToken(refreshToken);
    }

    // ============================================================
    // INTERNAL
    // ============================================================

    private void fakeDelay() {
        try {
            Thread.sleep(150 + (int) (Math.random() * 100));
        } catch (InterruptedException ignored) {
        }
    }

    private void checkForLock(User user) {

        if (user.isCurrentlyLocked()) {
            throw new ForbiddenException(
                    "Account locked until " + user.getLockUntil()
            );
        }
    }
}

