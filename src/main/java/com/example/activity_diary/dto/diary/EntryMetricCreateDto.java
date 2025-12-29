package com.example.activity_diary.dto.diary;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntryMetricCreateDto {

    @NotNull(message = "metricId is required")
    @Positive(message = "metricId must be positive")
    private Long metricId;

    @NotNull(message = "unitId is required")
    @Positive(message = "unitId must be positive")
    private Long unitId;

    @NotNull(message = "value is required")
    @Positive(message = "value must be at least 1")
    private Integer value;
}
