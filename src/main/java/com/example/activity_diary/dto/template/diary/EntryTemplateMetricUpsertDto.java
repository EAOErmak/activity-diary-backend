package com.example.activity_diary.dto.template.diary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EntryTemplateMetricUpsertDto {
    @NotNull
    Long metricTypeId;
    @Valid
    List<EntryTemplateMetricValueUpsertDto> values;
}
