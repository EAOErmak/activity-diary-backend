package com.example.activity_diary.dto.template;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EntryTemplateMetricValueViewDto {
    Long id;
    Long unitId;
    String unitName; // можно null
    Long value;
}
