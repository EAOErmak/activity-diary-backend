package com.example.activity_diary.service;

public interface VerificationService {
    void createAndSendCode(String username, Long chatId);
    boolean verify(String username, String inputCode);
}