package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.user.UserDto;
import com.example.activity_diary.dto.mapper.UserMapper;
import com.example.activity_diary.rate.RateLimit;
import com.example.activity_diary.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @RateLimit(capacity = 20, refillTokens = 20, refillPeriodSeconds = 60)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> me() {

        var user = userService.getCurrentUser();

        return ResponseEntity.ok(
                ApiResponse.success(userMapper.toDto(user))
        );
    }
}

