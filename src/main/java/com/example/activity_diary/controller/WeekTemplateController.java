package com.example.activity_diary.controller;

import com.example.activity_diary.dto.template.*;
import com.example.activity_diary.dto.template.week.WeekTemplateCreateDto;
import com.example.activity_diary.dto.template.week.WeekTemplateUpdateDto;
import com.example.activity_diary.dto.template.week.WeekTemplateViewDto;
import com.example.activity_diary.security.LightUserDetails;
import com.example.activity_diary.service.diary.WeekTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/week-templates")
public class WeekTemplateController {

    private final WeekTemplateService service;

    @GetMapping
    public Page<WeekTemplateViewDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return service.list(user.getId(), page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WeekTemplateViewDto create(
            @RequestBody WeekTemplateCreateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return service.create(user.getId(), dto);
    }

    @GetMapping("/{id}")
    public WeekTemplateViewDto get(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return service.get(user.getId(), id);
    }

    @PutMapping("/{id}")
    public WeekTemplateViewDto update(
            @PathVariable Long id,
            @RequestBody WeekTemplateUpdateDto dto,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        return service.update(user.getId(), id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal LightUserDetails user
    ) {
        service.delete(user.getId(), id);
    }
}
