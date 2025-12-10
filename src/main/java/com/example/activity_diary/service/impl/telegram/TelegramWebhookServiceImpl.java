package com.example.activity_diary.service.impl.telegram;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.service.telegram.TelegramService;
import com.example.activity_diary.service.telegram.TelegramWebhookService;
import com.example.activity_diary.service.auth.VerificationService;
import com.example.activity_diary.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Override
    public void processUpdate(Map<String, Object> update) {

        Long chatId = extractChatId(update);
        String text = extractMessage(update);

        if (chatId == null || text == null || text.isBlank()) return;

        // START
        if (text.equalsIgnoreCase("/start")) {
            telegramService.sendMessage(
                    chatId,
                    "Привет! Чтобы привязать Telegram к вашему профилю, отправь команду:\n" +
                            "/verify ваш_username"
            );
            return;
        }

        // VERIFY username
        if (text.toLowerCase().startsWith("/verify")) {
            String[] parts = text.split("\\s+", 2);

            if (parts.length < 2) {
                telegramService.sendMessage(chatId, "Формат команды:\n/verify ваш_username");
                return;
            }

            String username = parts[1].trim();
            handleUsername(chatId, username);
            return;
        }

        telegramService.sendMessage(
                chatId,
                "Не понимаю команду.\nИспользуйте: /verify ваш_username"
        );
    }


    private void handleUsername(Long chatId, String username) {

        String normalized = username.trim().toLowerCase();

        // Проверяем наличие пользователя
        var opt = userRepository.findByUsername(normalized);
        if (opt.isEmpty()) {
            telegramService.sendMessage(
                    chatId,
                    "Пользователь с таким username не найден.\n" +
                            "Убедитесь, что вы вводите username, указанный при регистрации."
            );
            return;
        }

        User user = opt.get();

        // 1. Проверяем что chatId уже не привязан к другому пользователю
        User existingByChat = userRepository.findByChatId(chatId).orElse(null);
        if (existingByChat != null && !existingByChat.getId().equals(user.getId())) {
            telegramService.sendMessage(
                    chatId,
                    "Этот Telegram уже привязан к аккаунту: " + existingByChat.getUsername()
            );
            return;
        }

        // 2. Если username привязан к другому Telegram
        if (user.getChatId() != null && !user.getChatId().equals(chatId)) {
            telegramService.sendMessage(
                    chatId,
                    "Этот username уже привязан к другому Telegram аккаунту."
            );
            return;
        }

        // 3. Привязываем chatId, если нужно
        if (user.getChatId() == null) {
            user.bindChatId(chatId);
            userRepository.save(user);
        }

        // 4. Отправляем код
        try {
            verificationService.createAndSendCode(normalized, chatId);

            telegramService.sendMessage(
                    chatId,
                    "Код подтверждения отправлен!\n" +
                            "Введите его на сайте, чтобы завершить регистрацию."
            );
        } catch (Exception e) {
            log.error("Error sending verification code", e);
            telegramService.sendMessage(chatId, "Ошибка при создании кода. Попробуйте позже.");
        }
    }

    @SuppressWarnings("unchecked")
    private Long extractChatId(Map<String, Object> update) {
        try {
            if (update == null) return null;

            Object msgObj = update.get("message");
            if (!(msgObj instanceof Map)) return null;

            Map<String, Object> message = (Map<String, Object>) msgObj;

            Object chatObj = message.get("chat");
            if (!(chatObj instanceof Map)) return null;

            Map<String, Object> chat = (Map<String, Object>) chatObj;

            Object idObj = chat.get("id");
            if (!(idObj instanceof Number)) return null;

            return ((Number) idObj).longValue();

        } catch (Exception e) {
            log.warn("extractChatId error: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractMessage(Map<String, Object> update) {
        try {
            if (update == null) return "";

            Object msgObj = update.get("message");
            if (!(msgObj instanceof Map)) return "";

            Map<String, Object> message = (Map<String, Object>) msgObj;

            Object text = message.get("text");
            return text == null ? "" : text.toString().trim();

        } catch (Exception e) {
            log.warn("extractMessage error: {}", e.getMessage());
            return "";
        }
    }
}
