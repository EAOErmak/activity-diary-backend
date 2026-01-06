package com.example.activity_diary.dto.diary.metric;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntryMetricValueUpdateDto {

    @NotNull
    @Positive
    private Long unitId;

    @NotNull
    @Positive
    private Integer value;
}

