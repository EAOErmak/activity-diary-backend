package com.example.activity_diary.platform.api.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.dto.analytics.MetricUsageAggDto;
import com.example.activity_diary.dto.analytics.MetricUsageAggFilterDto;
import com.example.activity_diary.dto.analytics.TagUsageAggDto;
import com.example.activity_diary.dto.analytics.TagUsageAggFilterDto;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.service.analytics.AnalyticsService;
import com.example.activity_diary.service.analytics.MetricUsageAggService;
import com.example.activity_diary.service.analytics.TagUsageAggService;
import com.example.activity_diary.service.analytics.TagChartTypeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Validated
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final TagUsageAggService tagUsageAggService;
    private final MetricUsageAggService metricUsageAggService;
    private final TagChartTypeService tagChartTypeService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/charts")
    public ApiResponse<ChartResponseDto> getChart(
            @ModelAttribute ChartFilterDto filter
    ) {
        return ApiResponse.success(
                analyticsService.getChart(currentUserProvider.getCurrentUserId(), filter)
        );
    }

    @GetMapping("/tag-usage")
    public ApiResponse<List<TagUsageAggDto>> getTagUsage(
            @Valid @ModelAttribute TagUsageAggFilterDto filter
    ) {
        return ApiResponse.success(
                tagUsageAggService.getUsage(currentUserProvider.getCurrentUserId(), filter)
        );
    }

    @GetMapping("/metric-usage")
    public ApiResponse<List<MetricUsageAggDto>> getMetricUsage(
            @Valid @ModelAttribute MetricUsageAggFilterDto filter
    ) {
        return ApiResponse.success(
                metricUsageAggService.getUsage(currentUserProvider.getCurrentUserId(), filter)
        );
    }

    @GetMapping("/tags/{tagId}/chart-types")
    public ApiResponse<List<ChartType>> getChartTypesByTag(
            @PathVariable @Positive Long tagId
    ) {
        return ApiResponse.success(tagChartTypeService.getChartTypesByTagId(tagId));
    }
}
