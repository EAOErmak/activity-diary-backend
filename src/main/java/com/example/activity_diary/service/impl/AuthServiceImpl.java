package com.example.activity_diary.service.impl;

import com.example.activity_diary.dto.AuthRequestDto;
import com.example.activity_diary.dto.AuthResponseDto;
import com.example.activity_diary.dto.RegisterRequestDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.ForbiddenException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.security.JwtUtils;
import com.example.activity_diary.service.AuthService;
import com.example.activity_diary.service.TelegramService;
import com.example.activity_diary.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final TelegramService telegramService;
    private final JwtUtils jwtUtils;

    @Override
    public AuthResponseDto register(RegisterRequestDto req) {
        String email = req.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName().trim())
                .enabled(false)
                .build();

        userRepository.save(user);

        // Возвращаем только инфо — без токенов, без verifyLink
        return new AuthResponseDto(
                null,
                null,
                user.getEmail(),
                user.getId(),
                null
        );
    }

    @Override
    public void requestVerificationCode(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getEnabled()) {
            throw new BadRequestException("Account already verified");
        }

        verificationService.createAndSendCode(user.getEmail(), user.getChatId());
    }

    @Override
    public boolean verifyCode(String email, String code) {
        boolean ok = verificationService.verify(email.trim().toLowerCase(), code);
        if (!ok) return false;

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);

        return true;
    }


    @Override
    public AuthResponseDto login(AuthRequestDto req) {
        String email = req.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        if (!user.getEnabled()) {
            throw new ForbiddenException("Account not verified");
        }

        String accessToken = jwtUtils.generateAccessToken(email);
        String refreshToken = jwtUtils.generateRefreshToken(email);

        return new AuthResponseDto(
                accessToken,
                refreshToken,
                email,
                user.getId(),
                null
        );
    }

    @Override
    public AuthResponseDto refresh(String refreshToken) {
        if (!jwtUtils.isValid(refreshToken)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        String email = jwtUtils.extractEmail(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getEnabled()) {
            throw new ForbiddenException("User not verified");
        }

        return new AuthResponseDto(
                jwtUtils.generateAccessToken(email),
                jwtUtils.generateRefreshToken(email),
                email,
                user.getId(),
                null
        );
    }
}
