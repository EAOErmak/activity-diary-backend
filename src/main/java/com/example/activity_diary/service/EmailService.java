package com.example.activity_diary.service;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
}
