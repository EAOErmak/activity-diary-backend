package com.example.activity_diary.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.rate.RateLimit;
import com.example.activity_diary.service.admin.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping
    @RateLimit(capacity = 10, refillTokens = 10, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<Map<String, Object>>> stats() {
        return ResponseEntity.ok(
                ApiResponse.ok(adminStatsService.getStats())
        );
    }
}
