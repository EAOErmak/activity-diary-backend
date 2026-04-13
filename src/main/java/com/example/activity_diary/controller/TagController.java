package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.security.CurrentUserProvider;
import com.example.activity_diary.service.diary.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagDto>>> getTags(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(
                ApiResponse.success(tagService.getVisibleTags(currentUserProvider.getCurrentUserId(), q))
        );
    }
}
