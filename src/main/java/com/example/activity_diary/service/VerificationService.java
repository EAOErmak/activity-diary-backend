package com.example.activity_diary.service;

public interface VerificationService {
    String createAndSendCode(String email, Long chatId);
    boolean verify(String email, String inputCode);
}