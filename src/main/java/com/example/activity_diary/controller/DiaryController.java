package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.rate.RateLimit;
import com.example.activity_diary.security.CustomUserDetails;
import com.example.activity_diary.security.LightUserDetails;
import com.example.activity_diary.service.diary.DiaryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
@Validated
public class DiaryController {

    private final DiaryService diaryService;

    // ============================================================
    // GET MY ENTRIES (PAGED)
    // ============================================================

    @RateLimit(capacity = 30, refillTokens = 30, refillPeriodSeconds = 30)
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<Page<DiaryEntryDto>>> myEntries(
            @AuthenticationPrincipal LightUserDetails user,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<DiaryEntryDto> result =
                diaryService.getMyEntries(user.getId(), pageable);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ============================================================
    // GET ONE
    // ============================================================

    @RateLimit(capacity = 20, refillTokens = 20, refillPeriodSeconds = 30)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DiaryEntryDto>> getById(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        DiaryEntryDto dto =
                diaryService.getMyEntryById(id, user.getId());

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    // ============================================================
    // CREATE
    // ============================================================

    @RateLimit(capacity = 30, refillTokens = 30, refillPeriodSeconds = 60)
    @PostMapping
    public ResponseEntity<ApiResponse<DiaryEntryDto>> create(
            @Valid @RequestBody DiaryEntryCreateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        DiaryEntryDto created =
                diaryService.create(dto, user.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @RateLimit(capacity = 15, refillTokens = 15, refillPeriodSeconds = 60)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DiaryEntryDto>> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DiaryEntryUpdateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        DiaryEntryDto updated =
                diaryService.update(id, dto, user.getId());

        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    // ============================================================
    // DELETE (LOGICAL)
    // ============================================================

    @RateLimit(capacity = 10, refillTokens = 10, refillPeriodSeconds = 60)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        diaryService.delete(id, user.getId());

        return ResponseEntity.noContent().build();
    }
}
