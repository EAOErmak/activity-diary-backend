package com.example.activity_diary.service.impl;

import com.example.activity_diary.service.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final RestTemplate restTemplate;

    @Override
    public void sendMessage(Long chatId, String message) {
        if (chatId == null) {
            throw new IllegalArgumentException("chatId must not be null");
        }
        if (message == null || message.isBlank()) {
            return;
        }

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", message);
        body.put("parse_mode", "HTML");

        restTemplate.postForObject(url, body, String.class);
    }

    @Override
    public void sendVerificationCode(Long chatId, String code) {
        sendMessage(chatId, "Ваш код подтверждения: <b>" + code + "</b>\nСрок действия: <i>60 секунд</i>.");
    }
}
