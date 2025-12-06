package com.example.activity_diary.controller.admin;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.diary.EntryFieldConfigDto;
import com.example.activity_diary.service.admin.AdminEntryFieldConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/entry-config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEntryFieldConfigController {

    private final AdminEntryFieldConfigService service;

    @PostMapping
    public ApiResponse<EntryFieldConfigDto> create(@RequestBody EntryFieldConfigDto dto) {
        return ApiResponse.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<EntryFieldConfigDto> update(
            @PathVariable Long id,
            @RequestBody EntryFieldConfigDto dto
    ) {
        return ApiResponse.ok(service.update(id, dto));
    }

    @GetMapping
    public ApiResponse<List<EntryFieldConfigDto>> getAll() {
        return ApiResponse.ok(service.getAll());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok(null);
    }
}


