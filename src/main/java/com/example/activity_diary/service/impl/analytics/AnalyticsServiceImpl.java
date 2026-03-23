package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.service.analytics.AnalyticsService;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final Map<ChartType, ChartCalculationStrategy> strategies;

    @Autowired
    public AnalyticsServiceImpl(List<ChartCalculationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(ChartCalculationStrategy::getChartType, s -> s));
    }

    @Override
    public ChartResponseDto getChart(Long userId, ChartFilterDto filter){
        ChartCalculationStrategy strategy = strategies.get(filter.getChartType());
        return strategy.calculate(userId, filter);
    }
}