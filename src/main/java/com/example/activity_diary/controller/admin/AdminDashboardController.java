package com.example.activity_diary.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.admin.AdminDashboardStatsDto;
import com.example.activity_diary.rate.RateLimit;
import com.example.activity_diary.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    @RateLimit(capacity = 10, refillTokens = 10, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<AdminDashboardStatsDto>> stats() {
        return ResponseEntity.ok(
                ApiResponse.ok(adminDashboardService.getStats())
        );
    }
}
