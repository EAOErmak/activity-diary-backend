package com.example.activity_diary.dto.diary;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityItemCreateDto {

    @NotNull(message = "nameId is required")
    @Min(value = 1, message = "nameId must be a positive ID")
    private Long nameId;

    @NotNull(message = "unitId is required")
    @Min(value = 1, message = "unitId must be a positive ID")
    private Long unitId;

    @NotNull(message = "count is required")
    @Min(value = 1, message = "count must be at least 1")
    private Integer count;
}
