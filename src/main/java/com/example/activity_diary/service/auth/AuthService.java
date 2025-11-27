package com.example.activity_diary.service.auth;

import com.example.activity_diary.dto.auth.AuthRequestDto;
import com.example.activity_diary.dto.auth.AuthResponseDto;
import com.example.activity_diary.dto.auth.RegisterRequestDto;

public interface AuthService {
    AuthResponseDto register(RegisterRequestDto req, String ip);
    void requestVerificationCode(String username);
    boolean verifyCode(String username, String code);
    AuthResponseDto login(AuthRequestDto req, String ip, String userAgent);
    AuthResponseDto refresh(String refreshToken);
    AuthResponseDto confirmLogin(String username, String code);
}
