package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.dictionary.DictionaryOptionDto;
import com.example.activity_diary.security.CurrentUserProvider;
import com.example.activity_diary.service.dictionary.DictionaryService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dictionary/metric-names")
@RequiredArgsConstructor
@Validated
public class DictionaryMetricNameController {

    private final DictionaryService dictionaryService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/{metricNameId}/units")
    public ResponseEntity<ApiResponse<List<DictionaryOptionDto>>> getUnitsByMetricName(@PathVariable @Positive Long metricNameId) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        dictionaryService.getUnitsByMetricNameId(
                                metricNameId,
                                currentUserProvider.getCurrentUser().getRole()
                        )
                )
        );
    }
}
