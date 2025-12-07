package com.example.activity_diary.dto.sync;

import com.example.activity_diary.entity.enums.SyncEntityType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class SyncStateResponseDto {

    private Map<SyncEntityType, Long> state;
}
