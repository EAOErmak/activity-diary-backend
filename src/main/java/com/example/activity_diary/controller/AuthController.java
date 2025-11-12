package com.example.activity_diary.controller;

import com.example.activity_diary.dto.AuthRequest;
import com.example.activity_diary.dto.AuthResponse;
import com.example.activity_diary.dto.RegisterRequest;
import com.example.activity_diary.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        AuthResponse resp = authService.register(req);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        AuthResponse resp = authService.login(req);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token) {
        boolean ok = authService.verifyToken(token);
        if (ok) return ResponseEntity.ok("Verified");
        return ResponseEntity.badRequest().body("Invalid or expired token");
    }
}
