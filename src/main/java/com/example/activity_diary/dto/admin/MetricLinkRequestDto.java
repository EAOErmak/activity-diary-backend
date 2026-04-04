package com.example.activity_diary.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MetricLinkRequestDto {

    @NotNull
    @Positive
    private Long metricNameId;

    @NotNull
    @Positive
    private Long metricUnitId;
}
