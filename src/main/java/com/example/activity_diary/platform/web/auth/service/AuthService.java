package com.example.activity_diary.platform.web.auth.service;

import com.example.activity_diary.dto.auth.AuthRequestDto;
import com.example.activity_diary.dto.auth.AuthResponseDto;
import com.example.activity_diary.dto.auth.RegisterRequestDto;
import com.example.activity_diary.dto.auth.RegisterResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {

    // Р РµРіРёСЃС‚СЂР°С†РёСЏ
    RegisterResponseDto register(RegisterRequestDto req, HttpServletRequest request);

    @Transactional
    AuthResponseDto login(AuthRequestDto req, HttpServletRequest request);

    // РћР±РЅРѕРІР»РµРЅРёРµ access РїРѕ refresh
    AuthResponseDto refresh(String refreshToken);

    void logout(String refreshToken);
}
