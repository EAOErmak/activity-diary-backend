package com.example.activity_diary.service.auth;

import com.example.activity_diary.entity.User;

public interface VerificationService {
    void createAndSendEmailVerification(User user, String email);
    void verifyEmailByToken(String token);
}