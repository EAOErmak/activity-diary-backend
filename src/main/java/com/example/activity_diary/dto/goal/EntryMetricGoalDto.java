package com.example.activity_diary.dto.goal;

import lombok.Data;

import java.util.List;

@Data
public class EntryMetricGoalDto {
    Long id;
    Integer position;

    Long metricTypeId;
    String metricTypeName; // если надо

    List<EntryMetricValueGoalDto> values;
}

