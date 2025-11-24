package com.example.activity_diary.dto.diary;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityItemDto {

    private Long id; // response-only, никаких аннотаций

    @NotNull(message = "dictNameId cannot be null")
    @Min(value = 1, message = "dictNameId must be a positive ID")
    private Long dictNameId;   // FK → activity_item_name_dict

    @NotNull(message = "dictUnitId cannot be null")
    @Min(value = 1, message = "dictUnitId must be a positive ID")
    private Long dictUnitId;   // FK → activity_item_unit_dict

    @NotNull(message = "count cannot be null")
    @Min(value = 1, message = "count must be greater than zero")
    private Integer count;
}

