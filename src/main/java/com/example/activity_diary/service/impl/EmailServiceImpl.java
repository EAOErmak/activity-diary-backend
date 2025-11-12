package com.example.activity_diary.service.impl;

import com.example.activity_diary.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationEmail(String to, String token) {
        String verifyUrl = "http://localhost:8080/api/auth/verify?token=" + token;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Verify your Activity Diary account");
        msg.setText("Click to verify: " + verifyUrl);
        mailSender.send(msg);
    }
}
