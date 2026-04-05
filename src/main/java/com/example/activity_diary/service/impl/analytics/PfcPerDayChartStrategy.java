package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import org.springframework.stereotype.Service;

@Service
public class PfcPerDayChartStrategy implements ChartCalculationStrategy {

    private final FoodChartSupportService foodChartSupportService;

    public PfcPerDayChartStrategy(FoodChartSupportService foodChartSupportService) {
        this.foodChartSupportService = foodChartSupportService;
    }

    @Override
    public ChartResponseDto calculate(Long userId, ChartFilterDto filter) {
        return foodChartSupportService.buildPfcPerDay(getChartType(), userId, filter);
    }

    @Override
    public ChartType getChartType() {
        return ChartType.PFC_PER_DAY;
    }
}
