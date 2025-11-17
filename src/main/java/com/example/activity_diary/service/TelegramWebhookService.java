package com.example.activity_diary.service;

import java.util.Map;

public interface TelegramWebhookService {
    void processUpdate(Map<String, Object> update);
}
