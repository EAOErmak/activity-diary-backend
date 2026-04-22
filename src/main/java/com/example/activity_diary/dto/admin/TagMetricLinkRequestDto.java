package com.example.activity_diary.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TagMetricLinkRequestDto {

    @NotNull
    @Positive
    private Long tagId;

    @NotNull
    @Positive
    private Long metricNameId;
}
