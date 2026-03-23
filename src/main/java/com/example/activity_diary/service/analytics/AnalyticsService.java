package com.example.activity_diary.service.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;

public interface AnalyticsService {
    ChartResponseDto getChart(Long userId, ChartFilterDto filter);
}