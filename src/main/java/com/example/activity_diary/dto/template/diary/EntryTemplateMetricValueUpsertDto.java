package com.example.activity_diary.dto.template.diary;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EntryTemplateMetricValueUpsertDto {
    @NotNull
    Long unitId;
    @NotNull
    Integer value;
}
