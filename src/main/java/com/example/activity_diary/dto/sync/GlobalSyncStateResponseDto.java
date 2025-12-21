package com.example.activity_diary.dto.sync;

import com.example.activity_diary.entity.enums.GlobalSyncEntityType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class GlobalSyncStateResponseDto {
    private Map<GlobalSyncEntityType, Long> state;
}
