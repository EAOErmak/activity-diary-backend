package com.example.activity_diary.controller;

import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.service.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // =========================================================
    // ====================== BY TIME ==========================
    // =========================================================

    @GetMapping("/time/category/{whatHappenedId}")
    public ChartResponseDto getByTimeByCategory(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long whatHappenedId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        return analyticsService.buildByTimeByCategory(
                ud,
                whatHappenedId,
                from,
                to
        );
    }

    @GetMapping("/time/what/{whatId}")
    public ChartResponseDto getByTimeByWhat(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long whatId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        return analyticsService.buildByTimeByWhat(
                ud,
                whatId,
                from,
                to
        );
    }

    // =========================================================
    // ==================== BY SEQUENCE ========================
    // =========================================================

    @GetMapping("/sequence/category/{whatHappenedId}")
    public ChartResponseDto getBySequenceByCategory(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long whatHappenedId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        return analyticsService.buildBySequenceByCategory(
                ud,
                whatHappenedId,
                from,
                to
        );
    }

    @GetMapping("/sequence/what/{whatId}")
    public ChartResponseDto getBySequenceByWhat(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long whatId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        return analyticsService.buildBySequenceByWhat(
                ud,
                whatId,
                from,
                to
        );
    }
}
