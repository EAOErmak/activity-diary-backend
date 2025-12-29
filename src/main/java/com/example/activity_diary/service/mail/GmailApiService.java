package com.example.activity_diary.service.mail;

import jakarta.mail.internet.MimeMessage;

public interface GmailApiService {

    void sendEmail(String to, String subject, String bodyText) throws Exception;

    MimeMessage createEmail(String to, String from, String subject, String body) throws Exception;
}
