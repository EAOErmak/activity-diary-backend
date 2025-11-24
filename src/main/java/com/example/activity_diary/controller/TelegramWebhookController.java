package com.example.activity_diary.controller;

import com.example.activity_diary.rate.RateLimit;
import com.example.activity_diary.service.telegram.TelegramWebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramWebhookController {

    private final TelegramWebhookService telegramWebhookService;
    private final ObjectMapper objectMapper;

    @RateLimit(capacity = 20, refillTokens = 20, refillPeriodSeconds = 1)
    @PostMapping(
            value = "/webhook",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE}
    )
    public ResponseEntity<Void> onUpdate(@RequestBody String rawBody) {
        try {
            Map<String, Object> update = objectMapper.readValue(rawBody, Map.class);
            telegramWebhookService.processUpdate(update);
        } catch (Exception e) {
            log.error("Failed to parse webhook update", e);
        }

        return ResponseEntity.ok().build();
    }
}
