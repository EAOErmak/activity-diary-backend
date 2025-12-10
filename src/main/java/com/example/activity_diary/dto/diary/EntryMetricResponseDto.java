package com.example.activity_diary.dto.diary;

import lombok.Data;

@Data
public class EntryMetricResponseDto {

    private Long id;

    // ✅ metricType
    private Long metricTypeId;
    private String metricTypeName;

    // ✅ unit
    private Long unitId;
    private String unitName;

    // ✅ value
    private Integer value;
}
