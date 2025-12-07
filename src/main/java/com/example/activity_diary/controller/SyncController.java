package com.example.activity_diary.controller;

import com.example.activity_diary.dto.sync.SyncStateResponseDto;
import com.example.activity_diary.service.sync.UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SyncController {

    private final UserSyncService syncService;

    @RestController
    @RequestMapping("/sync")
    @RequiredArgsConstructor
    public class SyncController {

        private final UserSyncService syncService;

        @GetMapping("/state")
        public SyncStateResponseDto getState(
                @AuthenticationPrincipal Long userId
        ) {
            return new SyncStateResponseDto(
                    syncService.getState(userId)
            );
        }
    }
}
