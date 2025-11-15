package com.example.activity_diary.service.impl;

import com.example.activity_diary.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramServiceImpl implements TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final RestTemplate restTemplate;

    @Override
    public void sendMessage(Long chatId, String message) {

        try {
            String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);

            String url = "https://api.telegram.org/bot"
                    + botToken
                    + "/sendMessage?chat_id=" + chatId
                    + "&text=" + encoded;

            restTemplate.getForObject(url, String.class);

        } catch (Exception e) {
            log.error("Failed to send message to Telegram: {}", e.getMessage());
        }
    }
}
