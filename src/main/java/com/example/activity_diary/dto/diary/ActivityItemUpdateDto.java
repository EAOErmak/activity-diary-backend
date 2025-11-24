package com.example.activity_diary.dto.diary;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityItemUpdateDto {

    @NotNull(message = "id is required for updating item")
    private Long id;

    @NotNull(message = "nameId is required")
    private Long nameId;
    @NotNull(message = "unitId is required")
    private Long unitId;

    @Min(value = 1, message = "count must be at least 1")
    private Integer count;
}
