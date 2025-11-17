package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.UserDto;
import com.example.activity_diary.dto.mappers.UserMapper;
import com.example.activity_diary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> me(@AuthenticationPrincipal UserDetails ud) {
        var user = userService.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        return ResponseEntity.ok(
                ApiResponse.success(userMapper.toDto(user))
        );
    }
}
