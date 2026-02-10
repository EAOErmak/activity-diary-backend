package com.example.activity_diary.dto.diary.metric;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EntryMetricUpdateDto {

    @Positive
    private Long id;

    @NotNull
    @Positive
    private Long metricTypeId;

    @NotNull
    @Size(min = 1)
    private List<EntryMetricValueUpdateDto> values;
}

