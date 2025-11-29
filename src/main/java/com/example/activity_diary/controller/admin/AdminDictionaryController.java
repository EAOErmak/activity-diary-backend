package com.example.activity_diary.controller.admin;

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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dict")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDictionaryController {

    private final DictionaryService dictionaryService;

    @PostMapping("/what-happened")
    @RateLimit(capacity = 10, refillTokens = 10, refillPeriodSeconds = 60)
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> createWhatHappened(
            @Valid @RequestBody DictionaryCreateDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.createWhatHappened(dto))
        );
    }

    @PutMapping("/what-happened/{id}")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> updateWhatHappened(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DictionaryUpdateDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.updateWhatHappened(id, dto))
        );
    }

    @PostMapping("/what")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> createWhat(
            @Valid @RequestBody DictionaryCreateDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        dictionaryService.createWhat(dto.getParentId(), dto)
                )
        );
    }

    @PutMapping("/what/{id}")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> updateWhat(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DictionaryUpdateDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.updateWhat(id, dto))
        );
    }

    @PostMapping("/item-name")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> createItemName(
            @Valid @RequestBody DictionaryCreateDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.createItemName(dto))
        );
    }

    @PutMapping("/item-name/{id}")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> updateItemName(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DictionaryUpdateDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.updateItemName(id, dto))
        );
    }

    @PostMapping("/unit")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> createUnit(
            @Valid @RequestBody DictionaryCreateDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.createUnit(dto))
        );
    }

    @PutMapping("/unit/{id}")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> updateUnit(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DictionaryUpdateDto dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.updateUnit(id, dto))
        );
    }
}
