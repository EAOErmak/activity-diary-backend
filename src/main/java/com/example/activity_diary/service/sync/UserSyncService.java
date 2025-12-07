package com.example.activity_diary.service.sync;

import com.example.activity_diary.entity.enums.SyncEntityType;

import java.util.Map;

public interface UserSyncService {
    void initUser(Long userId);
    void bump(Long userId, SyncEntityType type);
    Map<SyncEntityType, Long> getStateByUsername(String username);

}

