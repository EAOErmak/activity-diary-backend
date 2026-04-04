package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.service.analytics.AnalyticsService;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import com.example.activity_diary.service.analytics.TagChartTypeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final Map<ChartType, ChartCalculationStrategy> strategies;
    private final TagChartTypeService tagChartTypeService;
    private final ChartFilterValidator chartFilterValidator;

    public AnalyticsServiceImpl(
            List<ChartCalculationStrategy> strategyList,
            TagChartTypeService tagChartTypeService,
            ChartFilterValidator chartFilterValidator
    ) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(ChartCalculationStrategy::getChartType, s -> s));
        this.tagChartTypeService = tagChartTypeService;
        this.chartFilterValidator = chartFilterValidator;
    }

    @Override
    public ChartResponseDto getChart(Long userId, ChartFilterDto filter){
        chartFilterValidator.validate(filter);
        tagChartTypeService.validateChartTypeAllowed(filter.getTagId(), filter.getChartType());

        ChartCalculationStrategy strategy = strategies.get(filter.getChartType());
        if (strategy == null) {
            throw new BadRequestException("Chart type " + filter.getChartType() + " is not supported");
        }

        return strategy.calculate(userId, filter);
    }
}
