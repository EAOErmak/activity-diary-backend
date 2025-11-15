package com.example.activity_diary.service;

import com.example.activity_diary.dto.AuthRequest;
import com.example.activity_diary.dto.AuthResponse;
import com.example.activity_diary.dto.RegisterRequest;
import com.example.activity_diary.dto.VerifyCodeRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest req);
    AuthResponse login(AuthRequest req);
    boolean linkTelegramByToken(String verifyToken, Long chatId);
}
