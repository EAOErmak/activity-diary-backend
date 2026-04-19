package com.example.activity_diary.dto.template.diary;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EntryTemplateMetricValueUpsertDto {
    @NotNull
    Long unitId;
    @NotNull
    @Positive
    BigDecimal value;
}
