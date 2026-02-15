package com.example.activity_diary.dto.goal;

import lombok.Data;

@Data
public class EntryMetricValueGoalDto {
    Long id;

    Long unitId;
    String unitName; // если надо

    Integer expectedValue;
}
