package com.example.activity_diary.platform.api.controller;

import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.sync.GlobalSyncStateResponseDto;
import com.example.activity_diary.dto.sync.UserSyncStateResponseDto;
import com.example.activity_diary.service.sync.GlobalSyncService;
import com.example.activity_diary.service.sync.UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final UserSyncService userSyncService;
    private final GlobalSyncService globalSyncService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/user")
    public ApiResponse<UserSyncStateResponseDto> getState() {
        return ApiResponse.ok(
                userSyncService.getStateDto(currentUserProvider.getCurrentUserId())
        );
    }

    @GetMapping("/global")
    public ApiResponse<GlobalSyncStateResponseDto> global() {
        return ApiResponse.ok(
                globalSyncService.getStateDto()
        );
    }
}
