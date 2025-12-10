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

    @GetMapping("/time/category/{categoryId}")
    public ChartResponseDto getByTimeByCategory(
            @AuthenticationPrincipal UserDetails ud,
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
            @AuthenticationPrincipal UserDetails ud,
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

    // =========================================================
    // ==================== BY SEQUENCE ========================
    // =========================================================

    @GetMapping("/sequence/category/{categoryId}")
    public ChartResponseDto getBySequenceByCategory(
            @AuthenticationPrincipal UserDetails ud,
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
            @AuthenticationPrincipal UserDetails ud,
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

    // =========================================================
    // =============== USER ID EXTRACTION ======================
    // =========================================================

    private Long extractUserId(UserDetails ud) {
        // ❗ ТОЛЬКО ОДИН ВАРИАНТ ДОЛЖЕН ОСТАТЬСЯ

        // ✅ ЕСЛИ username = это ID:
        // return Long.parseLong(ud.getUsername());

        // ✅ ЕСЛИ используешь кастомный CustomUserDetails:
        return ((com.example.activity_diary.security.CustomUserDetails) ud).getId();
    }
}
