package com.example.activity_diary.service.sync;

import com.example.activity_diary.dto.sync.SyncStateResponseDto;
import com.example.activity_diary.entity.enums.SyncEntityType;

import java.util.Map;

public interface UserSyncService {

    void initUser(Long userId);

    void bump(Long userId, SyncEntityType type);

    // ✅ ВНУТРЕННИЙ метод (для сервисов)
    Map<SyncEntityType, Long> getState(Long userId);

    // ✅ ВНЕШНИЙ метод (для контроллеров)
    SyncStateResponseDto getStateDto(Long userId);
}
