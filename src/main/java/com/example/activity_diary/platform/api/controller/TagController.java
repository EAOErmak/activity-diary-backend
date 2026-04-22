package com.example.activity_diary.platform.api.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.dictionary.DictionaryOptionDto;
import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.service.diary.TagMetricService;
import com.example.activity_diary.service.diary.TagService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
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
        User currentUser = currentUserProvider.getCurrentUser();
        return ResponseEntity.ok(
                ApiResponse.success(tagService.getVisibleTags(currentUser.getId(), currentUser.getRole(), q))
        );
    }

    @GetMapping("/{id}/metrics")
    public ResponseEntity<ApiResponse<List<DictionaryOptionDto>>> getMetricsByTag(@PathVariable @Positive Long id) {
        User currentUser = currentUserProvider.getCurrentUser();
        return ResponseEntity.ok(
                ApiResponse.ok(
                        tagMetricService.getMetricsByTagId(id, currentUser.getId(), currentUser.getRole())
                )
        );
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<List<DictionaryOptionDto>>> getMetricsByTags(
            @RequestParam(required = false) List<@Positive Long> tagIds
    ) {
        User currentUser = currentUserProvider.getCurrentUser();
        return ResponseEntity.ok(
                ApiResponse.ok(
                        tagMetricService.getMetricsByTagIds(tagIds, currentUser.getId(), currentUser.getRole())
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
