package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.dictionary.DictionaryCreateDto;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryUpdateDto;
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
@RequestMapping("/api/dict")
@RequiredArgsConstructor
@Validated
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @PostMapping("/what-happened")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimit(capacity = 5, refillTokens = 5, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> createWhatHappened(
            @Valid @RequestBody DictionaryCreateDto dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.createWhatHappened(dto)));
    }

    @GetMapping("/what-happened")
    @RateLimit(capacity = 60, refillTokens = 60, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getAllWhatHappened() {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.getAllWhatHappened()));
    }

    @PutMapping("/what-happened/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> updateWhatHappened(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DictionaryUpdateDto dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.updateWhatHappened(id, dto)));
    }

    @PostMapping("/what")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> createWhat(
            @Valid @RequestBody DictionaryCreateDto dto
    ) {
        Long parent = dto.getParentId();
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.createWhat(parent, dto)));
    }

    @GetMapping("/what")
    @RateLimit(capacity = 60, refillTokens = 60, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getWhatByParent(
            @RequestParam @Positive Long parentId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.getWhatByParent(parentId)));
    }

    @PutMapping("/what/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> updateWhat(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DictionaryUpdateDto dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.updateWhat(id, dto)));
    }

    @PostMapping("/item-name")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> createItemName(
            @Valid @RequestBody DictionaryCreateDto dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.createItemName(dto)));
    }

    @GetMapping("/item-name")
    @RateLimit(capacity = 60, refillTokens = 60, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getAllItemNames() {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.getAllItemNames()));
    }

    @PutMapping("/item-name/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> updateItemName(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DictionaryUpdateDto dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.updateItemName(id, dto)));
    }

    @PostMapping("/unit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> createUnit(
            @Valid @RequestBody DictionaryCreateDto dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.createUnit(dto)));
    }

    @GetMapping("/unit")
    @RateLimit(capacity = 60, refillTokens = 60, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getAllUnits() {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.getAllUnits()));
    }

    @PutMapping("/unit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> updateUnit(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DictionaryUpdateDto dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.updateUnit(id, dto)));
    }

    @GetMapping("/search")
    @RateLimit(capacity = 60, refillTokens = 60, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> search(
            @RequestParam String query
    ) {
        return ResponseEntity.ok(ApiResponse.ok(dictionaryService.search(query)));
    }
}
