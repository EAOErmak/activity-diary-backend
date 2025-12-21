package com.example.activity_diary.dto.sync;

import com.example.activity_diary.entity.enums.UserSyncEntityType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class UserSyncStateResponseDto {
    private Map<UserSyncEntityType, Long> state;
}
