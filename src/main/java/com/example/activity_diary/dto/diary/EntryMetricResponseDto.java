package com.example.activity_diary.dto.diary;

import lombok.Data;

@Data
public class EntryMetricResponseDto {

    private Long id;

    private Long metricTypeId;
    private String metricTypeName;

    private Long unitId;
    private String unitName;

    private Integer value;
}
