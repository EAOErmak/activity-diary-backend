package com.example.activity_diary.service.analytics;

import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.entity.enums.ChartType;

public interface ChartCalculationStrategy {
    ChartResponseDto calculate(Long userId, ChartFilterDto filter);
    ChartType getChartType();
}