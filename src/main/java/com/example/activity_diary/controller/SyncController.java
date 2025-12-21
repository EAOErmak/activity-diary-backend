package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.sync.GlobalSyncStateResponseDto;
import com.example.activity_diary.dto.sync.UserSyncStateResponseDto;
import com.example.activity_diary.security.LightUserDetails;
import com.example.activity_diary.service.sync.GlobalSyncService;
import com.example.activity_diary.service.sync.UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final UserSyncService syncService;
    private final GlobalSyncService globalSyncService;

    @GetMapping("/user")
    public ApiResponse<UserSyncStateResponseDto> getState(
            @AuthenticationPrincipal LightUserDetails userDetails
    ) {
        return ApiResponse.ok(
                syncService.getStateDto(userDetails.getId())
        );
    }
    @GetMapping("/global")
    public ApiResponse<GlobalSyncStateResponseDto> global() {
        return ApiResponse.ok(
                globalSyncService.getStateDto()
        );
    }
}

