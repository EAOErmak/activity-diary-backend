package com.example.activity_diary.dto.diary;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EntryMetricDto {

    private Long id;

    @NotNull(message = "metricId cannot be null")
    @Min(value = 1, message = "metricId must be a positive ID")
    private Long metricId;

    @NotNull(message = "unitId cannot be null")
    @Min(value = 1, message = "unitId must be a positive ID")
    private Long unitId;

    @NotNull(message = "value cannot be null")
    @Min(value = 1, message = "value must be greater than zero")
    private Integer value;
}
