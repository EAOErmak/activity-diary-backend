package com.example.activity_diary.dto.template.diary;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class EntryTemplateMetricViewDto {
    Long id;
    Long metricTypeId;
    String metricTypeName; // можно null если не хочешь тянуть словарь
    List<EntryTemplateMetricValueViewDto> values;
}
