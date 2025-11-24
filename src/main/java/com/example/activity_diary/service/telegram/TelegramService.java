package com.example.activity_diary.service.telegram;

public interface TelegramService {
    void sendMessage(Long chatId, String message);
    void sendVerificationCode(Long chatId, String code);
}
