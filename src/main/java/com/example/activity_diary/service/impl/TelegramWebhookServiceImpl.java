package com.example.activity_diary.service.impl;

import com.example.activity_diary.service.TelegramService;
import com.example.activity_diary.service.TelegramWebhookService;
import com.example.activity_diary.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramWebhookServiceImpl implements TelegramWebhookService {

    private final TelegramService telegramService;
    private final VerificationService verificationService;

    @Override
    public void processUpdate(Map<String, Object> update) {
        Long chatId = extractChatId(update);
        String text = extractMessage(update);

        if (chatId == null || text == null || text.isBlank()) {
            return;
        }

        // /start
        if (text.equalsIgnoreCase("/start")) {
            telegramService.sendMessage(
                    chatId,
                    "Привет! Чтобы получить код подтверждения, отправь команду:\n" +
                            "/verify ваш_email@example.com"
            );
            return;
        }

        // /verify email
        if (text.toLowerCase().startsWith("/verify")) {
            String[] parts = text.split("\\s+", 2);
            if (parts.length < 2) {
                telegramService.sendMessage(chatId, "Формат: /verify ваш_email@example.com");
                return;
            }
            String email = parts[1].trim();
            handleEmail(chatId, email);
            return;
        }

        // просто email без команды
        if (isEmail(text)) {
            handleEmail(chatId, text.trim());
            return;
        }

        // всё остальное — непонятная команда
        telegramService.sendMessage(
                chatId,
                "Не понимаю команду.\nИспользуйте: /verify ваш_email@example.com"
        );
    }

    private void handleEmail(Long chatId, String email) {
        try {
            verificationService.createAndSendCode(email, chatId);
            telegramService.sendMessage(
                    chatId,
                    "Код отправлен.\n" +
                            "Откройте сайт и введите этот код в форме верификации, чтобы завершить регистрацию."
            );
        } catch (Exception e) {
            log.error("Error while creating code for email {}", email, e);
            telegramService.sendMessage(
                    chatId,
                    "Не удалось создать код. Проверьте email или попробуйте позже."
            );
        }
    }

    @SuppressWarnings("unchecked")
    private Long extractChatId(Map<String, Object> update) {
        try {
            Map<String, Object> message = (Map<String, Object>) update.get("message");
            Map<String, Object> chat = (Map<String, Object>) message.get("chat");
            return ((Number) chat.get("id")).longValue();
        } catch (Exception e) {
            log.warn("Cannot extract chatId from update: {}", update, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractMessage(Map<String, Object> update) {
        try {
            Map<String, Object> message = (Map<String, Object>) update.get("message");
            Object text = message.get("text");
            return text == null ? "" : text.toString().trim();
        } catch (Exception e) {
            log.warn("Cannot extract text from update: {}", update, e);
            return "";
        }
    }

    private boolean isEmail(String text) {
        return text.contains("@") && text.contains(".");
    }
}
