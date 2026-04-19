package com.example.activity_diary.dto.goal;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EntryMetricValueGoalDto {
    Long id;

    Long unitId;
    String unitName; // если надо

    BigDecimal expectedValue;
}
