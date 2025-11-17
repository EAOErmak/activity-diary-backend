package com.example.activity_diary.controller;

import com.example.activity_diary.service.TelegramWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
public class TelegramWebhookController {

    private final TelegramWebhookService telegramWebhookService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> onUpdate(@RequestBody Map<String, Object> update) {
        telegramWebhookService.processUpdate(update);
        return ResponseEntity.ok().build();
    }
}
