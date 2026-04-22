package com.example.activity_diary.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class TagMetricLinkReplaceRequestDto {

    @NotNull
    private List<@Positive Long> metricNameIds;
}
