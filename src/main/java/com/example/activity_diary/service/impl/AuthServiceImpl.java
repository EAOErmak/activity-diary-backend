package com.example.activity_diary.service.impl;

import com.example.activity_diary.dto.AuthRequest;
import com.example.activity_diary.dto.AuthResponse;
import com.example.activity_diary.dto.RegisterRequest;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.VerificationToken;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.VerificationTokenRepository;
import com.example.activity_diary.service.AuthService;
import com.example.activity_diary.service.EmailService;
import com.example.activity_diary.service.UserService;
import com.example.activity_diary.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Override
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already used");
        }
        User u = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .enabled(false)
                .build();
        userRepository.save(u);

        // create token and send email
        String token = UUID.randomUUID().toString();
        VerificationToken vt = VerificationToken.builder()
                .token(token)
                .user(u)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        tokenRepository.save(vt);
        emailService.sendVerificationEmail(u.getEmail(), token);

        String jwt = jwtUtils.generateToken(u.getEmail());
        return new AuthResponse(jwt, u.getEmail());
    }

    @Override
    public AuthResponse login(AuthRequest req) {
        User u = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        if (!u.isEnabled()) throw new RuntimeException("Email not verified");
        String token = jwtUtils.generateToken(u.getEmail());
        return new AuthResponse(token, u.getEmail());
    }

    @Override
    public String createVerificationToken(String email) {
        User u = userRepository.findByEmail(email).orElseThrow();
        String token = UUID.randomUUID().toString();
        VerificationToken vt = VerificationToken.builder().token(token).user(u).expiryDate(LocalDateTime.now().plusHours(24)).build();
        tokenRepository.save(vt);
        return token;
    }

    @Override
    public boolean verifyToken(String token) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);

        // 🔸 Если токен не найден — возвращаем false
        if (optionalToken.isEmpty()) {
            return false;
        }

        VerificationToken vt = optionalToken.get();

        // 🔸 Проверка срока действия
        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(vt);
            return false;
        }

        // 🔸 Активируем пользователя
        User user = vt.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        // 🔸 Удаляем токен, чтобы нельзя было использовать повторно
        tokenRepository.delete(vt);

        return true;
    }

}
