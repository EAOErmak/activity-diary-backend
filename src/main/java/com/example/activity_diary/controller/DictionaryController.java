// src/main/java/com/example/activity_diary/controller/DictionaryController.java
package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.rate.RateLimit;
import com.example.activity_diary.service.dictionary.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
@Validated
public class DictionaryController {

    private final DictionaryService dictionaryService;

    // ============================
    // GET BY TYPE (USER)
    // ============================

    @GetMapping("/{type}")
    @RateLimit(capacity = 60, refillTokens = 60, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getByTypeOrParent(
            @PathVariable DictionaryType type,
            @RequestParam(required = false) Long parentId,
            @AuthenticationPrincipal UserDetails ud
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.getByTypeOrParent(type, parentId, ud))
        );
    }

    // ============================
    // SEARCH (USER)
    // ============================

    @GetMapping("/search")
    @RateLimit(capacity = 60, refillTokens = 60, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> search(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails ud
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.search(query, ud))
        );
    }
}
