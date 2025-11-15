package com.example.activity_diary.controller;

import com.example.activity_diary.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramWebhookController {

    private final AuthService authService;

    @PostMapping("/webhook")
    public ResponseEntity<String> onUpdate(@RequestBody Map<String, Object> update) {
        try {
            if (update.containsKey("message")) {
                Map<String, Object> message = (Map<String, Object>) update.get("message");
                Map<String, Object> chat = (Map<String, Object>) message.get("chat");
                Number chatIdNum = (Number) chat.get("id");
                Long chatId = chatIdNum.longValue();

                Object textObj = message.get("text");
                if (textObj != null) {
                    String text = textObj.toString().trim();

                    if (text.startsWith("/start")) {
                        String[] parts = text.split(" ");
                        String token = null;

                        if (parts.length >= 2) token = parts[1].trim();

                        if (token == null) {
                            String raw = text.substring(6).trim();
                            if (!raw.isEmpty()) token = raw;
                        }

                        if (token != null && !token.isEmpty()) {
                            boolean ok = authService.linkTelegramByToken(token, chatId);
                            if (ok) return ResponseEntity.ok("linked");
                            else return ResponseEntity.ok("invalid token"); // FIX
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Webhook handling error", e);
        }

        return ResponseEntity.ok("ignored");
    }
}
