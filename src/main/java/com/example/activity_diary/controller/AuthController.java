package com.example.activity_diary.controller;

import com.example.activity_diary.dto.*;
import com.example.activity_diary.dto.auth.*;
import com.example.activity_diary.rate.RateLimit;
import com.example.activity_diary.service.auth.AuthService;
import com.example.activity_diary.service.auth.VerificationService;
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
    private final VerificationService verificationService;

    @RateLimit(capacity = 3, refillTokens = 3, refillPeriodSeconds = 3600)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto req,
            HttpServletRequest request
    ) {
        RegisterResponseDto result = authService.register(req, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @RequestBody @Valid AuthRequestDto req,
            HttpServletRequest request
    ) {
        AuthResponseDto result = authService.login(req, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@RequestParam String token) {
        verificationService.verifyEmailByToken(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified"));
    }

    @RateLimit(capacity = 10, refillTokens = 10, refillPeriodSeconds = 60)
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refresh(
            @Valid @RequestBody RefreshTokenRequest req
    ) {
        AuthResponseDto result = authService.refresh(req.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody RefreshTokenRequest req
    ) {
        authService.logout(req.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

