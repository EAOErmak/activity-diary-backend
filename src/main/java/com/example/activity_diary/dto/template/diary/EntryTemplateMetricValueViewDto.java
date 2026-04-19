package com.example.activity_diary.dto.template.diary;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class EntryTemplateMetricValueViewDto {
    Long id;
    Long unitId;
    String unitName; // можно null
    BigDecimal value;
}
