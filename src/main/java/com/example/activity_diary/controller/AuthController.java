package com.example.activity_diary.controller;

import com.example.activity_diary.dto.*;
import com.example.activity_diary.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(
            @RequestBody RegisterRequestDto req,
            HttpServletRequest http
    ) {
        String realIp = http.getRemoteAddr();      // <-- IP получаем тут
        AuthResponseDto result = authService.register(req, realIp);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@RequestBody AuthRequestDto req) {
        AuthResponseDto result = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refresh(@RequestBody Map<String, String> req) {
        String token = req.get("refreshToken");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("refreshToken is required"));
        }

        return ResponseEntity.ok(ApiResponse.success(authService.refresh(token)));
    }

    // Telegram-only verification
    @PostMapping("/verification/request")
    public ResponseEntity<ApiResponse<Void>> requestVerification(@RequestBody VerificationRequestDto req) {
        // username передаётся → дальше телеграмм выдаёт код
        authService.requestVerificationCode(req.getUsername());
        return ResponseEntity.ok(ApiResponse.okMessage("Verification code sent through Telegram bot"));
    }

    @PostMapping("/verification/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm(@RequestBody VerificationConfirmDto req) {
        boolean ok = authService.verifyCode(req.getUsername(), req.getCode());
        if (!ok) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired verification code"));
        }

        return ResponseEntity.ok(ApiResponse.okMessage("Account verified successfully"));
    }
}
