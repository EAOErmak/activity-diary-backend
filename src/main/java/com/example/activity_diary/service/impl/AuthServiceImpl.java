package com.example.activity_diary.service.impl;

import com.example.activity_diary.dto.AuthRequest;
import com.example.activity_diary.dto.AuthResponse;
import com.example.activity_diary.dto.RegisterRequest;
import com.example.activity_diary.dto.VerifyCodeRequest;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.security.JwtUtils;
import com.example.activity_diary.service.AuthService;
import com.example.activity_diary.service.TelegramService;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${telegram.bot.name}")
    private String botName;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TelegramService telegramService;
    private final JwtUtils jwtUtils;

    @Override
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already used");
        }

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .enabled(false)
                .build();

        userRepository.save(user);

        // generate verify token
        String verifyToken = UUID.randomUUID().toString();
        user.setVerifyToken(verifyToken);
        userRepository.save(user);

        // build t.me link (frontend will open it)
        String link = "https://t.me/" + botName + "?start=" + verifyToken;

        // Return AuthResponse carrying link (token still null)
        AuthResponse resp = new AuthResponse(null, user.getEmail(), user.getId(), link);
        resp.setUserId(user.getId());            // extend AuthResponse (см. ниже)
        resp.setVerifyLink(link);
        return resp;
    }

    @Override
    public boolean linkTelegramByToken(String verifyToken, Long chatId) {
        var opt = userRepository.findByVerifyToken(verifyToken);
        if (opt.isEmpty()) return false;

        User user = opt.get();
        user.setChatId(chatId);
        user.setEnabled(true);         // activate
        user.setVerifyToken(null);     // one-time token
        userRepository.save(user);

        // optional: notify user via telegram that linking succeeded
        telegramService.sendMessage(chatId, "Ваш аккаунт успешно привязан к " + user.getEmail());
        return true;
    }

    @Override
    public AuthResponse login(AuthRequest req) {

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtils.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getId(), null);
    }
}
