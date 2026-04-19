package com.example.activity_diary.platform.api.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.user.ChangePasswordRequest;
import com.example.activity_diary.dto.user.ChangeUsernameRequest;
import com.example.activity_diary.dto.user.UpdateProfileRequest;
import com.example.activity_diary.dto.user.UserDto;
import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> me() {
        Long userId = currentUserProvider.getCurrentUserId();

        return ResponseEntity.ok(
                ApiResponse.success(userService.getProfile(userId))
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<?>> updateProfile(@RequestBody @Valid UpdateProfileRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        userService.updateProfile(request, userId);
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(userId)));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<?>> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        userService.changePassword(request, userId);
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(userId)));
    }

    @PutMapping("/username")
    public ResponseEntity<ApiResponse<?>> changeUsername(@RequestBody @Valid ChangeUsernameRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        userService.changeUsername(request, userId);
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(userId)));
    }
}
