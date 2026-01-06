package com.example.activity_diary.dto.diary.metric;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EntryMetricResponseDto {

    private Long id;

    private Long metricTypeId;
    private String metricTypeName;

    private List<EntryMetricValueResponseDto> values;
}

