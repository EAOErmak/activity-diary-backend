package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.rate.RateLimit;
import com.example.activity_diary.service.diary.DiaryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
@Validated
public class DiaryController {

    private final DiaryService diaryService;

    @RateLimit(capacity = 30, refillTokens = 30, refillPeriodSeconds = 30)
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<Page<DiaryEntryDto>>> myEntries(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Page<DiaryEntryDto> result = diaryService.getMyEntries(ud, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @RateLimit(capacity = 20, refillTokens = 20, refillPeriodSeconds = 30)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DiaryEntryDto>> getById(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal UserDetails ud
    ) {
        DiaryEntryDto dto = diaryService.getMyEntryById(id, ud);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @RateLimit(capacity = 30, refillTokens = 30, refillPeriodSeconds = 60)
    @PostMapping
    public ResponseEntity<ApiResponse<DiaryEntryDto>> create(
            @Valid @RequestBody DiaryEntryCreateDto dto,
            @AuthenticationPrincipal UserDetails ud
    ) {
        DiaryEntryDto created = diaryService.create(dto, ud);
        return ResponseEntity.ok(ApiResponse.ok(created));
    }

    @RateLimit(capacity = 15, refillTokens = 15, refillPeriodSeconds = 60)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DiaryEntryDto>> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DiaryEntryUpdateDto dto,
            @AuthenticationPrincipal UserDetails ud
    ) {
        DiaryEntryDto updated = diaryService.update(id, dto, ud);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @RateLimit(capacity = 10, refillTokens = 10, refillPeriodSeconds = 60)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal UserDetails ud
    ) {
        diaryService.delete(id, ud);
        return ResponseEntity.ok(ApiResponse.okMessage("Deleted successfully"));
    }
}
