package com.example.activity_diary.platform.web.controller;

import com.example.activity_diary.dto.*;
import com.example.activity_diary.dto.auth.*;
import com.example.activity_diary.platform.web.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
