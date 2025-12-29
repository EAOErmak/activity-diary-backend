package com.example.activity_diary.service.impl.auth;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.VerificationCode;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.VerificationCodeRepository;
import com.example.activity_diary.service.auth.VerificationService;
import com.example.activity_diary.service.mail.GmailApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final UserRepository userRepository;
    private final GmailApiService gmailApiService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    @Transactional
    public void createAndSendEmailVerification(User user, String email) {

        String token = UUID.randomUUID().toString();

        VerificationCode verificationCode = VerificationCode.builder()
                .verificationCode(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        verificationCodeRepository.save(verificationCode);

        String verificationLink =
                baseUrl + "/api/auth/verify?token=" + token;

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            gmailApiService.sendEmail(
                                    email,
                                    "Подтверждение регистрации",
                                    buildEmailBody(user, verificationLink)
                            );
                        } catch (Exception e) {
                            log.error("Failed to send verification email to {}", email, e);
                        }
                    }
                }
        );
    }

    @Override
    public void verifyEmailByToken(String token) {

        VerificationCode code = verificationCodeRepository
                .findByVerificationCode(token)
                .orElseThrow(() -> new BadRequestException("Invalid token"));

        if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationCodeRepository.delete(code);
            throw new BadRequestException("Token expired");
        }

        User user = code.getUser();

        if (user.isEnabled()) {
            verificationCodeRepository.delete(code);
            throw new BadRequestException(
                    "EMAIL_ALREADY_VERIFIED"
            );
        }

        user.enable();
        userRepository.save(user);

        verificationCodeRepository.delete(code);
    }

    private String buildEmailBody(User user, String link) {
        return """
        <html>
          <body style="font-family:Arial,sans-serif;background:#f4f5f7;padding:40px">
            <table width="100%%" cellpadding="0" cellspacing="0">
              <tr>
                <td align="center">
                  <table width="600" style="background:#fff;padding:40px;border-radius:8px">
                    <tr>
                      <td>
                        <h2>Подтвердите email</h2>
                        <p>Здравствуйте, %s</p>
                        <p>Для активации аккаунта нажмите кнопку:</p>
                        <p style="text-align:center">
                          <a href="%s"
                             style="background:#4f46e5;color:#fff;
                                    padding:12px 20px;border-radius:6px;
                                    text-decoration:none;">
                            Подтвердить email
                          </a>
                        </p>
                        <p style="font-size:12px;color:#6b7280">
                          Ссылка действует 24 часа
                        </p>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </body>
        </html>
        """.formatted(user.getDisplayName(), link);
    }
}

