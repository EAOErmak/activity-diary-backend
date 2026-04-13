package com.example.activity_diary.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.admin.TagChartTypeLinkRequestDto;
import com.example.activity_diary.dto.admin.TagChartTypeLinkResponseDto;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.service.admin.AdminTagChartTypeService;
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
@RequestMapping("/api/admin/tag-chart-types")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
@Slf4j
public class AdminTagChartTypeController {

    private final AdminTagChartTypeService adminTagChartTypeService;

    @PostMapping
    public ResponseEntity<ApiResponse<TagChartTypeLinkResponseDto>> create(
            @Valid @RequestBody TagChartTypeLinkRequestDto dto
    ) {
        log.info("Admin tag chart type create requested: tagId={}, chartType={}", dto.getTagId(), dto.getChartType());
        return ResponseEntity.ok(
                ApiResponse.ok(adminTagChartTypeService.createLink(dto))
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestParam @Positive Long tagId,
            @RequestParam ChartType chartType
    ) {
        log.info("Admin tag chart type delete requested: tagId={}, chartType={}", tagId, chartType);
        adminTagChartTypeService.deleteLink(tagId, chartType);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/tag/{tagId}")
    public ResponseEntity<ApiResponse<List<TagChartTypeLinkResponseDto>>> getChartTypesByTag(
            @PathVariable @Positive Long tagId
    ) {
        log.info("Admin tag chart types requested for tagId={}", tagId);
        return ResponseEntity.ok(
                ApiResponse.ok(adminTagChartTypeService.getChartTypesByTagId(tagId))
        );
    }
}
