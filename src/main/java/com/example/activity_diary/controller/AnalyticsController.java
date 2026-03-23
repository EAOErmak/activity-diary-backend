package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.security.LightUserDetails;
import com.example.activity_diary.service.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  @GetMapping("/charts")
    public ApiResponse<ChartResponseDto> getChart(
            @AuthenticationPrincipal LightUserDetails user,
            @ModelAttribute ChartFilterDto filter
  ){
      return ApiResponse.success(analyticsService.getChart(user.getId(), filter));
  }
}

