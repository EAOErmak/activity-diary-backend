package com.example.activity_diary.service.sync;

import com.example.activity_diary.dto.sync.UserSyncStateResponseDto;
import com.example.activity_diary.entity.enums.UserSyncEntityType;

import java.util.Map;

public interface UserSyncService {

    void initUser(Long userId);

    void bump(Long userId, UserSyncEntityType type);

    Map<UserSyncEntityType, Long> getState(Long userId);

    UserSyncStateResponseDto getStateDto(Long userId);
}
