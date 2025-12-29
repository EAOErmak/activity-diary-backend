package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.user.ChangePasswordRequest;
import com.example.activity_diary.dto.user.ChangeUsernameRequest;
import com.example.activity_diary.dto.user.UpdateProfileRequest;
import com.example.activity_diary.dto.user.UserDto;
import com.example.activity_diary.dto.mapper.UserMapper;
import com.example.activity_diary.rate.RateLimit;
import com.example.activity_diary.security.LightUserDetails;
import com.example.activity_diary.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @RateLimit(capacity = 20, refillTokens = 20, refillPeriodSeconds = 60)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> me(
            @AuthenticationPrincipal LightUserDetails user
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(userService.getProfile(user.getId()))
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<?>> updateProfile(
            @RequestBody @Valid UpdateProfileRequest request,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        userService.updateProfile(request, user.getId());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @RequestBody @Valid ChangePasswordRequest request,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        userService.changePassword(request, user.getId());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/username")
    public ResponseEntity<ApiResponse<?>> changeUsername(
            @RequestBody @Valid ChangeUsernameRequest request,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        userService.changeUsername(request, user.getId());
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}

