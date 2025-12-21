// src/main/java/com/example/activity_diary/controller/DictionaryController.java
package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.rate.RateLimit;
import com.example.activity_diary.security.CustomUserDetails;
import com.example.activity_diary.security.LightUserDetails;
import com.example.activity_diary.service.dictionary.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
@Validated
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping("/all")
    public  ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getAll(
            @AuthenticationPrincipal LightUserDetails user
    ) {
        Role role = user.getRole();

        return  ResponseEntity.ok(
                ApiResponse.ok(
                        dictionaryService.getAll(role)
                )
        );
    }

    // ============================
    // GET BY TYPE (USER)
    // ============================

    @GetMapping("/{type}")
    @RateLimit(capacity = 60, refillTokens = 60, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getForUser(
            @PathVariable DictionaryType type,
            @RequestParam(required = false) Long parentId,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        Role role = user.getRole();

        return ResponseEntity.ok(
                ApiResponse.ok(
                        dictionaryService.getForUser(type, parentId, role)
                )
        );
    }

    // ============================
    // SEARCH (USER)
    // ============================

    @GetMapping("/search")
    @RateLimit(capacity = 60, refillTokens = 60, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> search(
            @RequestParam String query,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        Role role = user.getRole();

        return ResponseEntity.ok(
                ApiResponse.ok(
                        dictionaryService.search(query, role)
                )
        );
    }
}
