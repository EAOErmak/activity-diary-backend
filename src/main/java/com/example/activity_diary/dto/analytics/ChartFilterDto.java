package com.example.activity_diary.dto.analytics;

import com.example.activity_diary.entity.enums.ChartType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChartFilterDto {
    private Long tagId;
    private Instant dateFrom;
    private Instant dateTo;
    private ChartType chartType;
}