package com.example.activity_diary.platform.api.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryViewDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.entity.enums.UiStatus;
import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.service.diary.DiaryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
@Validated
public class DiaryController {

    private final DiaryService diaryService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<Slice<DiaryEntryViewDto>>> myEntries(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "9") @Min(1) @Max(100) int size,
            @RequestParam(required = false) UiStatus uiStatus,
            @RequestParam(required = false) Instant now,
            @RequestParam(required = false, name = "tags") List<String> tags,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        Pageable pageable = PageRequest.of(
                page, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "whenStarted")
                        .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"))
        );

        Instant effectiveNow = (now != null) ? now : Instant.now();
        Long userId = currentUserProvider.getCurrentUserId();

        return ResponseEntity.ok(ApiResponse.success(
                diaryService.getMyEntriesFiltered(userId, uiStatus, effectiveNow, tags, from, to, pageable)
        ));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<DiaryEntryViewDto>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        diaryService.getAllEntries(currentUserProvider.getCurrentUserId())
                )
        );
    }

    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<DiaryEntryViewDto>>> getByRange(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        diaryService.getEntriesByDateRange(currentUserProvider.getCurrentUserId(), from, to)
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DiaryEntryDto>> getById(@PathVariable @Positive Long id) {
        DiaryEntryDto dto = diaryService.getMyEntryById(id, currentUserProvider.getCurrentUserId());

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DiaryEntryDto>> create(@Valid @RequestBody DiaryEntryCreateDto dto) {
        DiaryEntryDto created = diaryService.create(dto, currentUserProvider.getCurrentUserId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DiaryEntryDto>> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DiaryEntryUpdateDto dto
    ) {
        DiaryEntryDto updated =
                diaryService.update(id, dto, currentUserProvider.getCurrentUserId());

        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        diaryService.delete(id, currentUserProvider.getCurrentUserId());

        return ResponseEntity.noContent().build();
    }
}
