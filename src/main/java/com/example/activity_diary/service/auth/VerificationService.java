package com.example.activity_diary.service.auth;

public interface VerificationService {
    void createAndSendCode(String username, Long chatId);
    boolean verify(String username, String inputCode);
    void createAndSendLoginCode(String username, Long chatId);
    boolean verifyLoginCode(String username, String code);
}