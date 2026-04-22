package com.example.activity_diary.platform.api.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.admin.TagMetricLinkRequestDto;
import com.example.activity_diary.dto.admin.TagMetricLinkResponseDto;
import com.example.activity_diary.service.admin.AdminTagMetricLinkService;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin/tag-metric-links")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
@Slf4j
public class AdminTagMetricLinkController {

    private final AdminTagMetricLinkService adminTagMetricLinkService;

    @PostMapping
    public ResponseEntity<ApiResponse<TagMetricLinkResponseDto>> create(
            @Valid @RequestBody TagMetricLinkRequestDto dto
    ) {
        log.info("Admin tag metric link create requested: tagId={}, metricNameId={}",
                dto.getTagId(), dto.getMetricNameId());
        return ResponseEntity.ok(
                ApiResponse.ok(adminTagMetricLinkService.createLink(dto))
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestParam @Positive Long tagId,
            @RequestParam @Positive Long metricNameId
    ) {
        log.info("Admin tag metric link delete requested: tagId={}, metricNameId={}", tagId, metricNameId);
        adminTagMetricLinkService.deleteLink(tagId, metricNameId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/tag/{tagId}/metrics")
    public ResponseEntity<ApiResponse<List<TagMetricLinkResponseDto>>> getMetricsByTag(
            @PathVariable @Positive Long tagId
    ) {
        log.info("Admin tag metric links requested for tagId={}", tagId);
        return ResponseEntity.ok(
                ApiResponse.ok(adminTagMetricLinkService.getMetricsByTagId(tagId))
        );
    }
}
