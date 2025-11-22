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
    private final JwtUtils jwtUtils;

    private static final int MAX_ACCOUNTS_PER_IP = 10;

    @Override
    public AuthResponseDto register(RegisterRequestDto req, String registrationIp) {
        String username = req.getUsername().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already registered");
        }

        long accountsFromIp = userRepository.countByRegistrationIp(registrationIp);
        if (accountsFromIp >= MAX_ACCOUNTS_PER_IP) {
            throw new ForbiddenException("Too many accounts created from your IP");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName().trim())
                .enabled(false)
                .registrationIp(registrationIp)
                .chatId(null)
                .build();

        userRepository.save(user);

        // Возвращаем только инфо — без токенов, без verifyLink
        return new AuthResponseDto(
                null,
                null,
                user.getUsername(),
                user.getId()
        );
    }

    @Override
    public void requestVerificationCode(String username) {
        User user = userRepository.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getEnabled()) {
            throw new BadRequestException("Account already verified");
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


    @Override
    public AuthResponseDto login(AuthRequestDto req) {
        String username = req.getUsername().trim().toLowerCase();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        if (!user.getEnabled()) {
            throw new ForbiddenException("Account not verified");
        }

        String accessToken = jwtUtils.generateAccessToken(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);

        return new AuthResponseDto(
                accessToken,
                refreshToken,
                username,
                user.getId()
        );
    }

    @Override
    public AuthResponseDto refresh(String refreshToken) {
        if (!jwtUtils.isValid(refreshToken)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        String username = jwtUtils.extractUsername(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getEnabled()) {
            throw new ForbiddenException("User not verified");
        }

        return new AuthResponseDto(
                jwtUtils.generateAccessToken(username),
                jwtUtils.generateRefreshToken(username),
                username,
                user.getId()
        );
    }
}
