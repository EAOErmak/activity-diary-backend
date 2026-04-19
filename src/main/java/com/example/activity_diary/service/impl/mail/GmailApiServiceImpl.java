package com.example.activity_diary.service.impl.mail;

import com.example.activity_diary.service.mail.GmailApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@Profile("web")
@RequiredArgsConstructor
public class GmailApiServiceImpl implements GmailApiService {

    @Value("${google.api.client-id:}") private String clientId;
    @Value("${google.api.client-secret:}") private String clientSecret;
    @Value("${google.api.refresh-token:}") private String refreshToken;
    @Value("${app.gmail.sender:no-reply@example.local}") private String senderEmail;

    @Override
    public void sendEmail(String to, String subject, String bodyText) throws Exception {
        validateConfiguration();

        com.google.api.client.http.javanet.NetHttpTransport transport =
                new com.google.api.client.http.javanet.NetHttpTransport();
        com.google.api.client.json.gson.GsonFactory jsonFactory =
                new com.google.api.client.json.gson.GsonFactory();

        com.google.api.client.auth.oauth2.TokenResponse tokenResponse = new com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest(
                transport,
                jsonFactory,
                refreshToken, clientId, clientSecret).execute();

        com.google.api.services.gmail.Gmail service = new com.google.api.services.gmail.Gmail.Builder(
                transport,
                jsonFactory,
                request -> request.getHeaders().setAuthorization("Bearer " + tokenResponse.getAccessToken()))
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

    private void validateConfiguration() {
        if (clientId.isBlank() || clientSecret.isBlank() || refreshToken.isBlank()) {
            throw new IllegalStateException("Google mail integration is not configured");
        }
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
