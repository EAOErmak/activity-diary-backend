package com.example.activity_diary.service.impl.mail;

import com.example.activity_diary.service.mail.GmailApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class GmailApiServiceImpl implements GmailApiService {

    @Value("${google.api.client-id}") private String clientId;
    @Value("${google.api.client-secret}") private String clientSecret;
    @Value("${google.api.refresh-token}") private String refreshToken;
    @Value("${app.gmail.sender}") private String senderEmail;

    @Override
    public void sendEmail(String to, String subject, String bodyText) throws Exception {
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

        jakarta.mail.internet.MimeMessage emailContent = createEmail(to, senderEmail, subject, bodyText);

        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        emailContent.writeTo(buffer);

        String encodedEmail = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.toByteArray());

        com.google.api.services.gmail.model.Message message = new com.google.api.services.gmail.model.Message();
        message.setRaw(encodedEmail);

        com.google.api.services.gmail.model.Message result = service.users().messages().send("me", message).execute();

        System.out.println("Email sent successfully. Message ID: " + result.getId());
    }

    @Override
    public MimeMessage createEmail(String to, String from, String subject, String htmlBody) throws Exception {
        java.util.Properties props = new java.util.Properties();
        jakarta.mail.Session session = jakarta.mail.Session.getDefaultInstance(props, null);

        jakarta.mail.internet.MimeMessage email = new jakarta.mail.internet.MimeMessage(session);

        email.setFrom(new jakarta.mail.internet.InternetAddress(from));

        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new jakarta.mail.internet.InternetAddress(to));

        email.setSubject(subject, "UTF-8");
        email.setContent(htmlBody, "text/html; charset=UTF-8");

        return email;
    }
}