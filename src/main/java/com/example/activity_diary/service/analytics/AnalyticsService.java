package com.example.activity_diary.service.analytics;

import com.example.activity_diary.dto.analytics.ChartResponseDto;

import java.time.LocalDateTime;

public interface AnalyticsService {

    // по времени, сгруппировано по категории (whatHappened)
    ChartResponseDto buildByTimeByCategory(
            Long userId,
            Long categoryId,
            LocalDateTime from,
            LocalDateTime to
    );

    // по времени, по what
    ChartResponseDto buildByTimeBySubCategory(
            Long userId,
            Long subCategoryId,
            LocalDateTime from,
            LocalDateTime to
    );

    // по порядку записей, по категории
    ChartResponseDto buildBySequenceByCategory(
            Long userId,
            Long categoryId,
            LocalDateTime from,
            LocalDateTime to
    );

    // по порядку записей, по what
    ChartResponseDto buildBySequenceBySubCategory(
            Long userId,
            Long subCategoryId,
            LocalDateTime from,
            LocalDateTime to
    );
}
