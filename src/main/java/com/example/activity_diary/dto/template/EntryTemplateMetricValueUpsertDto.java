package com.example.activity_diary.dto.template;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EntryTemplateMetricValueUpsertDto {
    @NotNull
    Long unitId;
    @NotNull
    Integer value;
}
