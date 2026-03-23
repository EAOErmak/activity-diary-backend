package com.example.activity_diary.controller.template;

import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateCreateDto;
import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateUpdateDto;
import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateViewDto;
import com.example.activity_diary.security.LightUserDetails;
import com.example.activity_diary.service.diary.DiaryEntryTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/entry-templates")
public class DiaryEntryTemplateController {

    private final DiaryEntryTemplateService diaryEntryTemplateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiaryEntryTemplateViewDto create(
            @AuthenticationPrincipal LightUserDetails user,
            @RequestBody DiaryEntryTemplateCreateDto dto
    ) {
        return diaryEntryTemplateService.create(user.getId(), dto);
    }

    @PutMapping("/{templateId}")
    public DiaryEntryTemplateViewDto update(
            @AuthenticationPrincipal LightUserDetails user,
            @PathVariable Long templateId,
            @RequestBody DiaryEntryTemplateUpdateDto dto
    ) {
        return diaryEntryTemplateService.update(user.getId(), templateId, dto);
    }

    @GetMapping("/{templateId}")
    public DiaryEntryTemplateViewDto get(
            @AuthenticationPrincipal LightUserDetails user,
            @PathVariable Long templateId
    ) {
        return diaryEntryTemplateService.get(user.getId(), templateId);
    }

    @GetMapping
    public Page<DiaryEntryTemplateViewDto> list(
            @AuthenticationPrincipal LightUserDetails user,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return diaryEntryTemplateService.list(user.getId(), pageable);
    }

    @DeleteMapping("/{templateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal LightUserDetails user,
            @PathVariable Long templateId
    ) {
        diaryEntryTemplateService.delete(user.getId(), templateId);
    }
}
