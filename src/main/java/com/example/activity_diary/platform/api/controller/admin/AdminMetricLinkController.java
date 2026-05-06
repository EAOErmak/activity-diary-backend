package com.example.activity_diary.platform.api.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.PageResponseDto;
import com.example.activity_diary.dto.admin.MetricLinkRequestDto;
import com.example.activity_diary.dto.admin.MetricLinkResponseDto;
import com.example.activity_diary.service.admin.AdminMetricLinkService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/metric-links")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
@Slf4j
public class AdminMetricLinkController {

    private final AdminMetricLinkService adminMetricLinkService;

    @PostMapping
    public ResponseEntity<ApiResponse<MetricLinkResponseDto>> create(
            @Valid @RequestBody MetricLinkRequestDto dto
    ) {
        log.info("Admin metric link create requested: metricNameId={}, metricUnitId={}",
                dto.getMetricNameId(), dto.getMetricUnitId());
        return ResponseEntity.ok(
                ApiResponse.ok(adminMetricLinkService.createLink(dto))
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestParam @Positive Long metricNameId,
            @RequestParam @Positive Long metricUnitId
    ) {
        log.info("Admin metric link delete requested: metricNameId={}, metricUnitId={}", metricNameId, metricUnitId);
        adminMetricLinkService.deleteLink(metricNameId, metricUnitId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/metric-name/{id}/units")
    public ResponseEntity<ApiResponse<PageResponseDto<MetricLinkResponseDto>>> getUnitsByMetricName(
            @PathVariable @Positive Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        int safePage = Math.max(page, 0);
        int safeLimit = Math.min(Math.max(limit, 1), 100);

        log.info("Admin metric links requested for metricNameId={}, page={}, limit={}", id, safePage, safeLimit);
        return ResponseEntity.ok(
                ApiResponse.ok(adminMetricLinkService.getUnitsByMetricName(id, safePage, safeLimit))
        );
    }
}
