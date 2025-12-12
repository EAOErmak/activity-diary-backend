package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.sync.SyncStateResponseDto;
import com.example.activity_diary.security.LightUserDetails;
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

    @GetMapping("/state")
    public ApiResponse<SyncStateResponseDto> getState(
            @AuthenticationPrincipal LightUserDetails userDetails
    ) {
        return ApiResponse.ok(
                syncService.getStateDto(userDetails.getId())
        );
    }
}

