package com.example.activity_diary.dto.diary.metric;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EntryMetricValueResponseDto {

    private Long unitId;
    private String unitName;
    private BigDecimal value;
}

