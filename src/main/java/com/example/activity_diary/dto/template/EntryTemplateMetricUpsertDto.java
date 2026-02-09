package com.example.activity_diary.dto.template;

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
