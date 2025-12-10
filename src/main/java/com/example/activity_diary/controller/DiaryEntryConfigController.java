package com.example.activity_diary.controller;

import com.example.activity_diary.dto.ApiResponse;
import com.example.activity_diary.dto.diary.EntryFieldConfigDto;
import com.example.activity_diary.service.diary.EntryFieldConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diary/entry-config")
@RequiredArgsConstructor
public class DiaryEntryConfigController {

    private final EntryFieldConfigService service;

    @GetMapping("/{whatHappenedId}")
    public ApiResponse<EntryFieldConfigDto> get(@PathVariable Long whatHappenedId) {
        EntryFieldConfigDto dto = service.get(whatHappenedId);
        return ApiResponse.success(dto);
    }
}

