package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateCreateDto;
import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateUpdateDto;
import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateViewDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DiaryEntryTemplateService {

    DiaryEntryTemplateViewDto create(Long userId, DiaryEntryTemplateCreateDto dto);

    DiaryEntryTemplateViewDto update(Long userId, Long templateId, DiaryEntryTemplateUpdateDto dto);

    DiaryEntryTemplateViewDto get(Long userId, Long templateId);

    Page<DiaryEntryTemplateViewDto> list(Long userId, Pageable pageable);

    void delete(Long userId, Long templateId);
}
