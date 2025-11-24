package com.example.activity_diary.service.telegram;

import java.util.Map;

public interface TelegramWebhookService {
    void processUpdate(Map<String, Object> update);
}
