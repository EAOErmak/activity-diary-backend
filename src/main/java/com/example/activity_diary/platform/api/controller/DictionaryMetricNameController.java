package com.example.activity_diary.platform.api.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.PageResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryOptionDto;
import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.service.dictionary.DictionaryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dictionary/metric-names")
@RequiredArgsConstructor
@Validated
public class DictionaryMetricNameController {

    private final DictionaryService dictionaryService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/{metricNameId}/units")
    public ResponseEntity<ApiResponse<PageResponseDto<DictionaryOptionDto>>> getUnitsByMetricName(
            @PathVariable @Positive Long metricNameId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "6") @Min(1) @Max(50) int limit,
            @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        dictionaryService.getUnitsByMetricNameId(
                                metricNameId,
                                currentUserProvider.getCurrentUserRole(),
                                q,
                                PageRequest.of(page, limit)
                        )
                )
        );
    }
}
