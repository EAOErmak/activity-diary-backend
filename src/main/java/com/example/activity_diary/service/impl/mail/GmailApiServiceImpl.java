package com.example.activity_diary.service.impl.mail;


import com.example.activity_diary.service.mail.GmailApiService;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Message;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class GmailApiServiceImpl implements GmailApiService {

    @Value("${google.api.client-id}") private String clientId;
    @Value("${google.api.client-secret}") private String clientSecret;
    @Value("${google.api.refresh-token}") private String refreshToken;
    @Value("${app.gmail.sender}") private String senderEmail;

    @Override
    public void sendEmail(String to, String subject, String bodyText) throws Exception {
        // 1. Получаем новый Access Token по Refresh Token через HTTP (порт 443)
        com.google.api.client.auth.oauth2.TokenResponse tokenResponse = new com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(),
                refreshToken, clientId, clientSecret).execute();

        com.google.api.client.googleapis.auth.oauth2.GoogleCredential credential =
                new com.google.api.client.googleapis.auth.oauth2.GoogleCredential().setFromTokenResponse(tokenResponse);

        com.google.api.services.gmail.Gmail service = new com.google.api.services.gmail.Gmail.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(), credential)
                .setApplicationName("ActivityDiary").build();

        // 2. Формируем письмо (используем наш вспомогательный метод)
        jakarta.mail.internet.MimeMessage emailContent = createEmail(to, senderEmail, subject, bodyText);

        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        emailContent.writeTo(buffer);

        // Google требует Base64Url кодирование для поля raw
        String encodedEmail = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.toByteArray());

        // 3. Отправка через Google API Message
        com.google.api.services.gmail.model.Message message = new com.google.api.services.gmail.model.Message();
        message.setRaw(encodedEmail);

        // Выполняем отправку и получаем результат
                com.google.api.services.gmail.model.Message result = service.users().messages().send("me", message).execute();

        // Печатаем ID сообщения в консоль — если он появился, значит Google ПРИНЯЛ письмо
                System.out.println("Email sent successfully. Message ID: " + result.getId());
    }

    @Override
    public MimeMessage createEmail(String to, String from, String subject, String htmlBody) throws Exception {
        java.util.Properties props = new java.util.Properties();
        jakarta.mail.Session session = jakarta.mail.Session.getDefaultInstance(props, null);

        jakarta.mail.internet.MimeMessage email = new jakarta.mail.internet.MimeMessage(session);

        email.setFrom(new jakarta.mail.internet.InternetAddress(from));
        // Явно указываем jakarta.mail.Message, чтобы не путать с Google Message
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new jakarta.mail.internet.InternetAddress(to));

        email.setSubject(subject, "UTF-8");
        email.setContent(htmlBody, "text/html; charset=UTF-8");

        return email;
    }
}