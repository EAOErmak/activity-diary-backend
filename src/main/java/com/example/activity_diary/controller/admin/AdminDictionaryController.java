// src/main/java/com/example/activity_diary/controller/admin/AdminDictionaryController.java
package com.example.activity_diary.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.dictionary.DictionaryCreateDto;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryUpdateDto;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.rate.RateLimit;
import com.example.activity_diary.service.dictionary.DictionaryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dict")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminDictionaryController {

    private final DictionaryService dictionaryService;

    // ============================
    // GET BY TYPE (ADMIN)
    // ============================

    @GetMapping("/{type}")
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getByType(
            @PathVariable DictionaryType type
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.getByTypeForAdmin(type))
        );
    }

    // ============================
    // CREATE
    // ============================

    @PostMapping
    @RateLimit(capacity = 10, refillTokens = 10, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> create(
            @Valid @RequestBody DictionaryCreateDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.create(dto))
        );
    }

    // ============================
    // UPDATE
    // ============================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DictionaryUpdateDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.update(id, dto))
        );
    }

    // ============================
    // SEARCH (ADMIN)
    // ============================

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> search(
            @RequestParam String q
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.searchForAdmin(q))
        );
    }
}
