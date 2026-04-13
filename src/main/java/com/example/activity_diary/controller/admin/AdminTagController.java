package com.example.activity_diary.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.diary.TagCreateDto;
import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.service.admin.AdminTagService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminTagController {

    private final AdminTagService adminTagService;

    @PostMapping
    public ResponseEntity<ApiResponse<TagDto>> create(
            @Valid @RequestBody TagCreateDto dto
    ) {
        log.info("Admin tag create requested: name={}", dto.getName());
        return ResponseEntity.ok(
                ApiResponse.ok(adminTagService.create(dto))
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Slice<TagDto>>> myTags(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String q
    ) {
        log.info("Admin tags requested: page={}, size={}, q={}", page, size, q);
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Direction.ASC, "name")
                        .and(Sort.by(Sort.Direction.ASC, "id"))
        );

        return ResponseEntity.ok(ApiResponse.success(
                adminTagService.getTags(q, pageable)
        ));
    }

    @PostMapping("/{id}/approve")
    public void approve(@PathVariable Long id) {
        log.info("Admin tag approve requested: id={}", id);
        adminTagService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public void reject(@PathVariable Long id) {
        log.info("Admin tag reject requested: id={}", id);
        adminTagService.reject(id);
    }

    @PostMapping("/{id}/deprecate")
    public void deprecate(@PathVariable Long id) {
        log.info("Admin tag deprecate requested: id={}", id);
        adminTagService.deprecate(id);
    }
}
