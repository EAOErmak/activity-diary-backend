package com.example.activity_diary.controller;

import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.security.LightUserDetails;
import com.example.activity_diary.service.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PREMIUM')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/time/category/{categoryId}")
    public ChartResponseDto getByTimeByCategory(
            @AuthenticationPrincipal LightUserDetails ud,
            @PathVariable Long categoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        Long userId = extractUserId(ud);

        return analyticsService.buildByTimeByCategory(
                userId,
                categoryId,
                from,
                to
        );
    }

    @GetMapping("/time/sub-category/{subCategoryId}")
    public ChartResponseDto getByTimeBySubCategory(
            @AuthenticationPrincipal LightUserDetails ud,
            @PathVariable Long subCategoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        Long userId = extractUserId(ud);

        return analyticsService.buildByTimeBySubCategory(
                userId,
                subCategoryId,
                from,
                to
        );
    }

    @GetMapping("/sequence/category/{categoryId}")
    public ChartResponseDto getBySequenceByCategory(
            @AuthenticationPrincipal LightUserDetails ud,
            @PathVariable Long categoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        Long userId = extractUserId(ud);

        return analyticsService.buildBySequenceByCategory(
                userId,
                categoryId,
                from,
                to
        );
    }

    @GetMapping("/sequence/sub-category/{subCategoryId}")
    public ChartResponseDto getBySequenceBySubCategory(
            @AuthenticationPrincipal LightUserDetails ud,
            @PathVariable Long subCategoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        Long userId = extractUserId(ud);

        return analyticsService.buildBySequenceBySubCategory(
                userId,
                subCategoryId,
                from,
                to
        );
    }

    private Long extractUserId(UserDetails ud) {
        return ((com.example.activity_diary.security.LightUserDetails) ud).getId();
    }
}
