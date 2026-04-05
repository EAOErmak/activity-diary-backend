package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import org.springframework.stereotype.Service;

@Service
public class CaloriesPerDayChartStrategy implements ChartCalculationStrategy {

    private final FoodChartSupportService foodChartSupportService;

    public CaloriesPerDayChartStrategy(FoodChartSupportService foodChartSupportService) {
        this.foodChartSupportService = foodChartSupportService;
    }

    @Override
    public ChartResponseDto calculate(Long userId, ChartFilterDto filter) {
        return foodChartSupportService.buildCaloriesPerDay(getChartType(), userId, filter);
    }

    @Override
    public ChartType getChartType() {
        return ChartType.CALORIES_PER_DAY;
    }
}
