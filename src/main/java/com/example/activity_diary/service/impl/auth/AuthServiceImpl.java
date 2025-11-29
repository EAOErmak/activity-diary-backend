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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final RefreshTokenService refreshTokenService;
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
    public AuthResponseDto register(RegisterRequestDto req, String ip) {

        String username = req.getUsername().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            fakeDelay(); // одинаковое время
            throw new BadRequestException("Invalid username or password");
        }

        // Проверка лимита 10 аккаунтов на IP
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
                .chatId(req.getChatId())
                .build();

        userRepository.save(user);

        // FIX: сохраняем событие регистрации
        RegistrationEvent event = RegistrationEvent.builder()
                .user(user)
                .ip(ip)
                .build();

        registrationEventRepository.save(event);

        return AuthResponseDto.builder()
                .username(username)
                .userId(user.getId())
                .twoFactorRequired(true)
                .build();
    }


    // ============================================================
    // VERIFICATION
    // ============================================================

    @Override
    public void requestVerificationCode(String username) {
        String normalized = username.trim().toLowerCase();
        User user = userRepository.findByUsername(normalized).orElse(null);

        // Всегда одинаковый ответ
        if (user == null) {
            fakeDelay();
            return;
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            fakeDelay();
            return;
        }

        verificationService.createAndSendCode(user.getUsername(), user.getChatId());
    }


    @Override
    public boolean verifyCode(String username, String code) {

        boolean ok = verificationService.verify(username.trim().toLowerCase(), code);
        if (!ok) return false;

        User user = userRepository.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getChatId() == null) {
            throw new ForbiddenException("Telegram verification not completed");
        }

        user.setEnabled(true);
        userRepository.save(user);

        return true;
    }

    // ============================================================
    // LOGIN
    // ============================================================

    @Override
    public AuthResponseDto login(AuthRequestDto req, String ip, String userAgent) {

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

        if (!passwordMatches) {
            fakeDelay();
            loginEventService.recordFailure(ip, userAgent);
            throw new BadRequestException("Invalid username or password");
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            fakeDelay();
            loginEventService.recordFailure(ip, userAgent);
            throw new BadRequestException("Invalid username or password");
        }

        cheсkForLock(user);

        // SUCCESS
        loginEventService.recordSuccess(user.getId(), ip, userAgent);

        verificationService.createAndSendLoginCode(user.getUsername(), user.getChatId());

        return AuthResponseDto.builder()
                .twoFactorRequired(true)
                .username(username)
                .userId(user.getId())
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

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new ForbiddenException("User not verified");
        }

        cheсkForLock(user);

        loginEventService.recordSuccess(user.getId(), null, null);

        String accessToken = jwtUtils.generateAccessToken(user.getUsername());
        RefreshToken refreshToken = refreshTokenService.create(user);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getTokenHash())
                .username(user.getUsername())
                .userId(user.getId())
                .build();
    }

    // ============================================================
    // REFRESH (rotate-on-use)
    // ============================================================

    @Override
    public AuthResponseDto refresh(String rawRefreshToken) {

        RefreshToken newToken = refreshTokenService.verifyAndRotate(rawRefreshToken);

        User user = newToken.getUser();
        if (!user.getEnabled()) {
            throw new ForbiddenException("User not verified");
        }

        cheсkForLock(user);

        String accessToken = jwtUtils.generateAccessToken(user.getUsername());

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(newToken.getTokenHash())
                .username(user.getUsername())
                .userId(user.getId())
                .build();
    }

    private void fakeDelay() {
        try {
            // Фиксированная задержка 150–250ms, чтобы скрыть разницу в ответах
            Thread.sleep(150 + (int)(Math.random() * 100));
        } catch (InterruptedException ignored) {}
    }

    private void cheсkForLock(User user){
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            // ✅ если срок блокировки ещё не истёк — не пускаем
            if (user.getLockUntil() != null
                    && user.getLockUntil().isAfter(LocalDateTime.now())) {

                throw new ForbiddenException(
                        "Account locked until " + user.getLockUntil()
                );
            }

            // ✅ если срок ИСТЁК — автоматически разблокируем
            user.setAccountLocked(false);
            user.setLockUntil(null);
            user.setFailed2faAttempts(0);
            userRepository.save(user);
        }
    }
}
