package com.example.activity_diary.service.auth;

import com.example.activity_diary.dto.auth.AuthRequestDto;
import com.example.activity_diary.dto.auth.AuthResponseDto;
import com.example.activity_diary.dto.auth.RegisterRequestDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    // Регистрация
    AuthResponseDto register(RegisterRequestDto req, HttpServletRequest request);

    // Логин
    AuthResponseDto login(AuthRequestDto req, HttpServletRequest request);

    // Обновление access по refresh
    AuthResponseDto refresh(String refreshToken);

    // Подтверждение логина (2FA / Telegram)
    AuthResponseDto confirmLogin(String username, String code);

    // Запрос кода подтверждения
    void requestVerificationCode(String username);

    // Подтверждение верификации аккаунта
    void verifyCode(String username, String code);

    // ✅ КРИТИЧНО: logout с инвалидцией refresh-токена
    void logout(String refreshToken);
}

