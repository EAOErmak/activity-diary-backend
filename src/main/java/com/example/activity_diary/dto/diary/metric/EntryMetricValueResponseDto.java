package com.example.activity_diary.dto.diary.metric;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntryMetricValueResponseDto {

    private Long unitId;
    private String unitName;
    private Integer value;
}

