package com.example.activity_diary.service.impl.auth;

import com.example.activity_diary.dto.auth.AuthRequestDto;
import com.example.activity_diary.dto.auth.AuthResponseDto;
import com.example.activity_diary.dto.auth.RegisterRequestDto;
import com.example.activity_diary.dto.auth.RegisterResponseDto;
import com.example.activity_diary.entity.UserAccount;
import com.example.activity_diary.entity.enums.ProviderType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.entity.log.RegistrationEvent;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.RefreshToken;
import com.example.activity_diary.exception.types.UnauthorizedException;
import com.example.activity_diary.repository.RegistrationEventRepository;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.ForbiddenException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.UserAccountRepository;
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

import java.time.LocalDateTime;

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
    private final UserAccountRepository userAccountRepository;

    private static final int MAX_ACCOUNTS_PER_IP = 10;

    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoO5uG08CqslfQJz8l8bY5v3y5i7x3UXZG";

    //Регистрация
    @Override
    @Transactional
    public RegisterResponseDto register(RegisterRequestDto req, HttpServletRequest request) {

        String ip = IpUtils.getClientIp(request);

        String email = req.getEmail().trim().toLowerCase();
        String username = req.getUsername().trim().toLowerCase();

        // ============================
        // 1. Проверки
        // ============================

        if (userRepository.existsByUsername(username)) {
            fakeDelay();
            throw new BadRequestException("Username already taken");
        }

        if (userAccountRepository.existsByProviderAndProviderId(
                ProviderType.LOCAL, email)) {
            fakeDelay();
            throw new BadRequestException("Email already registered");
        }

        long accountsFromIp = registrationEventRepository.countByIp(ip);
        if (accountsFromIp >= MAX_ACCOUNTS_PER_IP) {
            fakeDelay();
            throw new ForbiddenException("Too many accounts created from your IP");
        }

        // ============================
        // 2. Создаём User (НЕ активен)
        // ============================

        User user = User.builder()
                .username(username)
                .fullName(req.getFullName())
                .enabled(false)
                .role(Role.USER)
                .build();

        userRepository.save(user);

        // ============================
        // 3. Создаём UserAccount (LOCAL)
        // ============================

        UserAccount account = UserAccount.builder()
                .user(user)
                .provider(ProviderType.LOCAL)
                .providerId(email) // email = логин
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .build();

        userAccountRepository.save(account);

        // ============================
        // 4. Email verification LINK
        // ============================

        verificationService.createAndSendEmailVerification(user, email);

        // ============================
        // 5. Аудит + sync
        // ============================

        registrationEventRepository.save(
                RegistrationEvent.builder()
                        .user(user)
                        .ip(ip)
                        .build()
        );

        userSyncService.initUser(user.getId());

        // ============================
        // 6. Ответ
        // ============================

        return RegisterResponseDto.builder()
                .message("Registration successful. Please verify your email.")
                .build();
    }

    @Transactional
    @Override
    public AuthResponseDto login(AuthRequestDto req, HttpServletRequest request) {
        String email = req.getEmail().trim().toLowerCase();

        // 1. Ищем локальный аккаунт по email
        UserAccount account = userAccountRepository
                .findByProviderAndProviderId(ProviderType.LOCAL, email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        User user = account.getUser();

        // 2. Проверяем, не заблокирован ли аккаунт (твоя логика из User.java)
        checkForLock(user);

        // 3. Проверяем пароль
        if (!passwordEncoder.matches(req.getPassword(), account.getPasswordHash())) {
            // Здесь можно добавить логику увеличения счетчика неудачных попыток (failed2faAttempts)
            user.increaseFailed2faAttempts();
            if (user.getFailed2faAttempts() >= 5) {
                user.lockUntil(LocalDateTime.now().plusMinutes(15));
            }
            userRepository.save(user);

            fakeDelay();
            throw new UnauthorizedException("Invalid email or password");
        }

        // 4. ПРОВЕРКА ВЕРИФИКАЦИИ (Самое важное!)
        if (!user.isEnabled()) {
            throw new ForbiddenException("EMAIL_NOT_VERIFIED");
        }

        // 5. Если всё ок — сбрасываем попытки и генерируем токены
        user.unlock();
        userRepository.save(user);

        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername(), user.getRole().name());

        // Сохраняем Refresh Token в базу
        refreshTokenService.save(user, refreshToken);

        // Логируем вход (твоя существующая логика)
        loginEventService.recordSuccess(user.getId(), IpUtils.getClientIp(request), request.getHeader("User-Agent"));

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .userId(user.getId())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponseDto refresh(String rawRefreshToken) {

        RefreshToken stored = refreshTokenService.verify(rawRefreshToken);

        User user = stored.getUser();

        if (!user.isEnabled()) {
            throw new ForbiddenException("User not verified");
        }

        checkForLock(user);

        refreshTokenService.revoke(stored);

        String newAccess = jwtUtils.generateAccessToken(
                user.getId(), user.getUsername(), user.getRole().name()
        );
        String newRefresh = jwtUtils.generateRefreshToken(
                user.getId(), user.getUsername(), user.getRole().name()
        );

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

