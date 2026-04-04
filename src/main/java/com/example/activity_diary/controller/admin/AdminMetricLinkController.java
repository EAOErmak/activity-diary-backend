package com.example.activity_diary.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.admin.MetricLinkRequestDto;
import com.example.activity_diary.dto.admin.MetricLinkResponseDto;
import com.example.activity_diary.service.admin.AdminMetricLinkService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin/metric-links")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminMetricLinkController {

    private final AdminMetricLinkService adminMetricLinkService;

    @PostMapping
    public ResponseEntity<ApiResponse<MetricLinkResponseDto>> create(
            @Valid @RequestBody MetricLinkRequestDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminMetricLinkService.createLink(dto))
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestParam @Positive Long metricNameId,
            @RequestParam @Positive Long metricUnitId
    ) {
        adminMetricLinkService.deleteLink(metricNameId, metricUnitId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/metric-name/{id}/units")
    public ResponseEntity<ApiResponse<List<MetricLinkResponseDto>>> getUnitsByMetricName(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminMetricLinkService.getUnitsByMetricName(id))
        );
    }
}
