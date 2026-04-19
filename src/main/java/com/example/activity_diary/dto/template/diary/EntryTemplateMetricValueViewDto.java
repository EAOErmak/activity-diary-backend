package com.example.activity_diary.dto.template.diary;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EntryTemplateMetricValueViewDto {
    Long id;
    Long unitId;
    String unitName; // РјРѕР¶РЅРѕ null
    Long value;
}
