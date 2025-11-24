package com.example.activity_diary.dto.diary;

import lombok.Data;

@Data
public class ActivityItemResponseDto {
    private Long id;
    private Long nameId;
    private String name;
    private Long unitId;
    private String unit;
    private Integer count;
}
