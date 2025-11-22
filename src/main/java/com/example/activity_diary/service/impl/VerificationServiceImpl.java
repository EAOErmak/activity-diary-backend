package com.example.activity_diary.service.impl;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.VerificationCode;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.VerificationCodeRepository;
import com.example.activity_diary.service.TelegramService;
import com.example.activity_diary.service.VerificationService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificationServiceImpl implements VerificationService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final UserRepository userRepository;
    private final TelegramService telegramService;

    private static final int CODE_TTL_MINUTES = 1;
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public void createAndSendCode(String username, Long chatId) {
        User user = userRepository.findByUsername(username.toLowerCase().trim())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (chatId == null) {
            throw new BadRequestException("chatId must not be null");
        }

        User existing = userRepository.findByChatId(chatId).orElse(null);

        if (existing != null && !existing.getId().equals(user.getId())) {
            throw new BadRequestException("Этот Telegram аккаунт уже привязан к другому профилю: " + existing.getUsername());
        }

        if (user.getChatId() == null || !user.getChatId().equals(chatId)) {
            user.setChatId(chatId);
            userRepository.save(user);
        }

        // Удаляем предыдущий код и гарантируем выполнение перед insert
        verificationCodeRepository.deleteByUserId(user.getId());
        verificationCodeRepository.flush();

        // Генерация кода
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        VerificationCode vc = VerificationCode.builder()
                .user(user)
                .verificationCode(code)
                .expiresAt(LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES))
                .attempts(MAX_ATTEMPTS)
                .build();

        verificationCodeRepository.save(vc);

        // Отправка в Telegram
        try {
            telegramService.sendVerificationCode(chatId, code);
        } catch (Exception e) {
            // В случае ошибки отменяем действие
            throw new RuntimeException("Failed to send Telegram message: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verify(String username, String inputCode) {
        User user = userRepository.findByUsername(username.toLowerCase().trim())
                .orElseThrow(() -> new NotFoundException("User not found"));

        VerificationCode vc = verificationCodeRepository.findByUserId(user.getId())
                .orElse(null);

        if (vc == null) {
            return false;
        }

        // Проверка срока действия и попыток
        if (vc.getAttempts() <= 0 || vc.getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationCodeRepository.delete(vc);
            return false;
        }

        // Неверный код
        if (!vc.getVerificationCode().equals(inputCode)) {
            vc.setAttempts(vc.getAttempts() - 1);
            verificationCodeRepository.save(vc);
            return false;
        }

        // Успешная верификация — просто удаляем код.
        // Включение пользователя (enabled = true) делает AuthServiceImpl.verifyCode.
        verificationCodeRepository.delete(vc);

        return true;
    }
}
