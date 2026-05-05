package com.example.activity_diary.platform.api.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.admin.TagMetricLinkReplaceRequestDto;
import com.example.activity_diary.dto.admin.TagMetricLinkResponseDto;
import com.example.activity_diary.dto.diary.TagCreateDto;
import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.dto.diary.TagUpdateDto;
import com.example.activity_diary.service.admin.AdminTagMetricLinkService;
import com.example.activity_diary.service.admin.AdminTagService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminTagController {

    private final AdminTagService adminTagService;
    private final AdminTagMetricLinkService adminTagMetricLinkService;

    @PostMapping
    public ResponseEntity<ApiResponse<TagDto>> create(
            @Valid @RequestBody TagCreateDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminTagService.create(dto))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TagDto>> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody TagUpdateDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminTagService.update(id, dto))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable @Positive Long id
    ) {
        adminTagService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Slice<TagDto>>> myTags(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String q
    ) {
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Direction.ASC, "name")
                        .and(Sort.by(Sort.Direction.ASC, "id"))
        );

        return ResponseEntity.ok(ApiResponse.success(
                adminTagService.getTags(q, pageable)
        ));
    }

    @PostMapping("/{id}/approve")
    public void approve(@PathVariable Long id) {
        adminTagService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public void reject(@PathVariable Long id) {
        adminTagService.reject(id);
    }

    @PostMapping("/{id}/deprecate")
    public void deprecate(@PathVariable Long id) {
        adminTagService.deprecate(id);
    }

    @GetMapping("/{id}/metrics")
    public ResponseEntity<ApiResponse<List<TagMetricLinkResponseDto>>> getMetrics(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminTagMetricLinkService.getMetricsByTagId(id))
        );
    }

    @PutMapping("/{id}/metrics")
    public ResponseEntity<ApiResponse<List<TagMetricLinkResponseDto>>> replaceMetrics(
            @PathVariable @Positive Long id,
            @Valid @RequestBody TagMetricLinkReplaceRequestDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminTagMetricLinkService.replaceLinks(id, dto.getMetricNameIds()))
        );
    }
}
