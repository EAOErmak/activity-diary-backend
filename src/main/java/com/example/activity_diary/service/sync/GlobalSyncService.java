package com.example.activity_diary.service.sync;

import com.example.activity_diary.dto.sync.GlobalSyncStateResponseDto;
import com.example.activity_diary.entity.enums.GlobalSyncEntityType;

import java.util.Map;

public interface GlobalSyncService {

    void initIfNeeded();

    void bump(GlobalSyncEntityType type);

    Map<GlobalSyncEntityType, Long> getState();

    GlobalSyncStateResponseDto getStateDto();
}
