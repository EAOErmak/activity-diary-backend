package com.example.activity_diary.service.auth;

public interface VerificationService {
    void createAndSendCode(String username, Long chatId);
    boolean verify(String username, String inputCode);
}