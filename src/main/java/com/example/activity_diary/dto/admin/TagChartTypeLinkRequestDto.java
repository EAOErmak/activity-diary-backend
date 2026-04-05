package com.example.activity_diary.dto.admin;

import com.example.activity_diary.entity.enums.ChartType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TagChartTypeLinkRequestDto {

    @NotNull
    @Positive
    private Long tagId;

    @NotNull
    private ChartType chartType;
}
