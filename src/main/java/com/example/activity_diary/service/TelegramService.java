package com.example.activity_diary.service;

public interface TelegramService {
    void sendMessage(Long chatId, String message);
}
