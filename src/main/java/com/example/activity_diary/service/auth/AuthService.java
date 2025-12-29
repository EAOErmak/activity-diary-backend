package com.example.activity_diary.service.auth;

import com.example.activity_diary.dto.auth.AuthRequestDto;
import com.example.activity_diary.dto.auth.AuthResponseDto;
import com.example.activity_diary.dto.auth.RegisterRequestDto;
import com.example.activity_diary.dto.auth.RegisterResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {

    // Регистрация
    RegisterResponseDto register(RegisterRequestDto req, HttpServletRequest request);

    @Transactional
    AuthResponseDto login(AuthRequestDto req, HttpServletRequest request);

    // Обновление access по refresh
    AuthResponseDto refresh(String refreshToken);

    void logout(String refreshToken);
}

