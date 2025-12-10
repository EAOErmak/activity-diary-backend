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
            HttpServletRequest request
    ) {
        AuthResponseDto result = authService.register(req, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @RateLimit(capacity = 5, refillTokens = 5, refillPeriodSeconds = 60)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody AuthRequestDto req,
            HttpServletRequest request
    ) {
        AuthResponseDto result = authService.login(req, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @RateLimit(capacity = 10, refillTokens = 10, refillPeriodSeconds = 60)
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refresh(
            @Valid @RequestBody RefreshTokenRequest req
    ) {
        AuthResponseDto result = authService.refresh(req.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @RateLimit(capacity = 5, refillTokens = 5, refillPeriodSeconds = 60)
    @PostMapping("/login/confirm")
    public ResponseEntity<ApiResponse<AuthResponseDto>> confirmLogin(
            @Valid @RequestBody VerificationConfirmDto req
    ) {
        AuthResponseDto result =
                authService.confirmLogin(req.getUsername(), req.getCode());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @RateLimit(capacity = 3, refillTokens = 3, refillPeriodSeconds = 60)
    @PostMapping("/verification/request")
    public ResponseEntity<ApiResponse<Void>> requestVerification(
            @Valid @RequestBody VerificationRequestDto req
    ) {
        authService.requestVerificationCode(req.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }

    @RateLimit(capacity = 10, refillTokens = 10, refillPeriodSeconds = 60)
    @PostMapping("/verification/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmVerification(
            @Valid @RequestBody VerificationConfirmDto req
    ) {
        authService.verifyCode(req.getUsername(), req.getCode());
        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }

    // ✅ КРИТИЧЕСКИ ВАЖНЫЙ ENDPOINT
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody RefreshTokenRequest req
    ) {
        authService.logout(req.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

