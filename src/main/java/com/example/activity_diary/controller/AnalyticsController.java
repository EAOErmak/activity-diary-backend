package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.dto.analytics.MetricUsageAggDto;
import com.example.activity_diary.dto.analytics.MetricUsageAggFilterDto;
import com.example.activity_diary.dto.analytics.TagUsageAggDto;
import com.example.activity_diary.dto.analytics.TagUsageAggFilterDto;
import com.example.activity_diary.security.LightUserDetails;
import com.example.activity_diary.service.analytics.AnalyticsService;
import com.example.activity_diary.service.analytics.MetricUsageAggService;
import com.example.activity_diary.service.analytics.TagUsageAggService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final TagUsageAggService tagUsageAggService;
    private final MetricUsageAggService metricUsageAggService;

    @GetMapping("/charts")
    public ApiResponse<ChartResponseDto> getChart(
            @AuthenticationPrincipal LightUserDetails user,
            @ModelAttribute ChartFilterDto filter
    ) {
        return ApiResponse.success(analyticsService.getChart(user.getId(), filter));
    }

    @GetMapping("/tag-usage")
    public ApiResponse<List<TagUsageAggDto>> getTagUsage(
            @AuthenticationPrincipal LightUserDetails user,
            @Valid @ModelAttribute TagUsageAggFilterDto filter
    ) {
        return ApiResponse.success(tagUsageAggService.getUsage(user.getId(), filter));
    }

    @GetMapping("/metric-usage")
    public ApiResponse<List<MetricUsageAggDto>> getMetricUsage(
            @AuthenticationPrincipal LightUserDetails user,
            @Valid @ModelAttribute MetricUsageAggFilterDto filter
    ) {
        return ApiResponse.success(metricUsageAggService.getUsage(user.getId(), filter));
    }
}

