package com.example.activity_diary.platform.api.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.dictionary.DictionaryCreateDto;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryUpdateDto;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.service.dictionary.DictionaryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AdminDictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping("/{type}")
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> getByType(
            @PathVariable DictionaryType type
    ) {
        log.info("Admin dictionary requested by type={}", type);
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.getByTypeForAdmin(type))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> create(
            @Valid @RequestBody DictionaryCreateDto dto
    ) {
        log.info("Admin dictionary create requested: type={}, label={}", dto.getType(), dto.getLabel());
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.create(dto))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DictionaryResponseDto>> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DictionaryUpdateDto dto
    ) {
        log.info("Admin dictionary update requested: id={}, active={}, allowedRole={}, label={}",
                id, dto.getActive(), dto.getAllowedRole(), dto.getLabel());
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.update(id, dto))
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DictionaryResponseDto>>> search(
            @RequestParam String q
    ) {
        log.info("Admin dictionary search requested: q={}", q);
        return ResponseEntity.ok(
                ApiResponse.ok(dictionaryService.searchForAdmin(q))
        );
    }
}
