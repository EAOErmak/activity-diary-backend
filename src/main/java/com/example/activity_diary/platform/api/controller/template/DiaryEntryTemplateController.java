package com.example.activity_diary.platform.api.controller.template;

import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateCreateDto;
import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateUpdateDto;
import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateViewDto;
import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.service.diary.DiaryEntryTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/entry-templates")
public class DiaryEntryTemplateController {

    private final DiaryEntryTemplateService diaryEntryTemplateService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiaryEntryTemplateViewDto create(@RequestBody DiaryEntryTemplateCreateDto dto) {
        return diaryEntryTemplateService.create(currentUserProvider.getCurrentUserId(), dto);
    }

    @PutMapping("/{templateId}")
    public DiaryEntryTemplateViewDto update(
            @PathVariable Long templateId,
            @RequestBody DiaryEntryTemplateUpdateDto dto
    ) {
        return diaryEntryTemplateService.update(currentUserProvider.getCurrentUserId(), templateId, dto);
    }

    @GetMapping("/{templateId}")
    public DiaryEntryTemplateViewDto get(@PathVariable Long templateId) {
        return diaryEntryTemplateService.get(currentUserProvider.getCurrentUserId(), templateId);
    }

    @GetMapping
    public Page<DiaryEntryTemplateViewDto> list(@PageableDefault(size = 20) Pageable pageable) {
        return diaryEntryTemplateService.list(currentUserProvider.getCurrentUserId(), pageable);
    }

    @DeleteMapping("/{templateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long templateId) {
        diaryEntryTemplateService.delete(currentUserProvider.getCurrentUserId(), templateId);
    }
}
