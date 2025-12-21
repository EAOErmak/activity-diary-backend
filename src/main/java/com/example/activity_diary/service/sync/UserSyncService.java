package com.example.activity_diary.service.sync;

import com.example.activity_diary.dto.sync.UserSyncStateResponseDto;
import com.example.activity_diary.entity.enums.UserSyncEntityType;

import java.util.Map;

public interface UserSyncService {

    void initUser(Long userId);

    void bump(Long userId, UserSyncEntityType type);

    // ✅ ВНУТРЕННИЙ метод (для сервисов)
    Map<UserSyncEntityType, Long> getState(Long userId);

    // ✅ ВНЕШНИЙ метод (для контроллеров)
    UserSyncStateResponseDto getStateDto(Long userId);
}
