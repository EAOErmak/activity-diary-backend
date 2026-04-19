package com.example.activity_diary.dto.goal;

import lombok.Data;

import java.util.List;

@Data
public class EntryMetricGoalDto {
    Long id;
    Long metricTypeId;
    String metricTypeName; // РµСЃР»Рё РЅР°РґРѕ

    List<EntryMetricValueGoalDto> values;
}
