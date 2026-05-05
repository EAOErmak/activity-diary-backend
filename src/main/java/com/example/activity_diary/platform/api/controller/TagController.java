package com.example.activity_diary.platform.api.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.PageResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryOptionDto;
import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.service.diary.TagMetricService;
import com.example.activity_diary.service.diary.TagService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Validated
public class TagController {

    private final TagService tagService;
    private final TagMetricService tagMetricService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagDto>>> getTags(@RequestParam(required = false) String q) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success(tagService.getVisibleTags(currentUserId, currentUserProvider.getCurrentUserRole(), q))
        );
    }

    @GetMapping("/{id}/metrics")
    public ResponseEntity<ApiResponse<List<DictionaryOptionDto>>> getMetricsByTag(@PathVariable @Positive Long id) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.ok(
                        tagMetricService.getMetricsByTagId(id, currentUserId, currentUserProvider.getCurrentUserRole())
                )
        );
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<PageResponseDto<DictionaryOptionDto>>> getMetricsByTags(
            @RequestParam(required = false) List<@Positive Long> tagIds,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "6") @Min(1) @Max(50) int limit,
            @RequestParam(required = false) String q
    ) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.ok(
                        tagMetricService.getMetricsByTagIds(
                                tagIds,
                                currentUserId,
                                currentUserProvider.getCurrentUserRole(),
                                q,
                                PageRequest.of(page, limit)
                        )
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
