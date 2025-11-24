package com.example.activity_diary.controller;

import com.example.activity_diary.dto.*;
import com.example.activity_diary.dto.auth.*;
import com.example.activity_diary.rate.RateLimit;
import com.example.activity_diary.service.auth.AuthService;
import com.example.activity_diary.util.IpUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @RateLimit(capacity = 3, refillTokens = 3, refillPeriodSeconds = 3600)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto req,
            HttpServletRequest http
    ) {
        String realIp = IpUtils.getClientIp(http);
        AuthResponseDto result = authService.register(req, realIp);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @RateLimit(capacity = 5, refillTokens = 5, refillPeriodSeconds = 60)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody AuthRequestDto req,
            HttpServletRequest http
    ) {
        String ip = http.getRemoteAddr();
        String userAgent = http.getHeader("User-Agent");

        AuthResponseDto result = authService.login(req, ip, userAgent);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @RateLimit(capacity = 10, refillTokens = 10, refillPeriodSeconds = 60)
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refresh(
            @Valid @RequestBody RefreshTokenRequest req
    ) {
        String token = req.getRefreshToken();
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(token)));
    }

    // Telegram-only verification
    @RateLimit(capacity = 3, refillTokens = 3, refillPeriodSeconds = 60)
    @PostMapping("/verification/request")
    public ResponseEntity<ApiResponse<Void>> requestVerification(
            @Valid @RequestBody VerificationRequestDto req
    ) {
        authService.requestVerificationCode(req.getUsername());
        return ResponseEntity.ok(ApiResponse.okMessage("Verification code sent through Telegram bot"));
    }

    @RateLimit(capacity = 10, refillTokens = 10, refillPeriodSeconds = 60)
    @PostMapping("/verification/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm(
            @Valid @RequestBody VerificationConfirmDto req
    ) {
        boolean ok = authService.verifyCode(req.getUsername(), req.getCode());
        if (!ok) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired verification code"));
        }

        return ResponseEntity.ok(ApiResponse.okMessage("Account verified successfully"));
    }
}
