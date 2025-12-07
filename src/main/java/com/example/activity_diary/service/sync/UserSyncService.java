package com.example.activity_diary.service.sync;

import com.example.activity_diary.dto.sync.SyncStateResponseDto;
import com.example.activity_diary.entity.enums.SyncEntityType;

import java.util.Map;

public interface UserSyncService {

    void initUser(Long userId);
    void bump(Long userId, SyncEntityType type);
    Map<SyncEntityType, Long> getState(Long userId);
    SyncStateResponseDto getStateByUsername(String username); // ✅ ВАЖНО
}


