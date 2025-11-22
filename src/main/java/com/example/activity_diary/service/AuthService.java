package com.example.activity_diary.service;

import com.example.activity_diary.dto.AuthRequestDto;
import com.example.activity_diary.dto.AuthResponseDto;
import com.example.activity_diary.dto.RegisterRequestDto;

public interface AuthService {
    AuthResponseDto register(RegisterRequestDto req, String registrationIp);
    void requestVerificationCode(String username);
    boolean verifyCode(String username, String code);
    AuthResponseDto login(AuthRequestDto req);
    AuthResponseDto refresh(String refreshToken);
}
