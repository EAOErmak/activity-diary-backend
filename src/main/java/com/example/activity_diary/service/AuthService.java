package com.example.activity_diary.service;

import com.example.activity_diary.dto.AuthRequest;
import com.example.activity_diary.dto.AuthResponse;
import com.example.activity_diary.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest req);
    AuthResponse login(AuthRequest req);
    String createVerificationToken(String email);
    boolean verifyToken(String token);
}
