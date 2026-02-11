package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.template.week.WeekTemplateCreateDto;
import com.example.activity_diary.dto.template.week.WeekTemplateUpdateDto;
import com.example.activity_diary.dto.template.week.WeekTemplateViewDto;
import org.springframework.data.domain.Page;

public interface WeekTemplateService {
    Page<WeekTemplateViewDto> list(Long userId, int page, int size);
    WeekTemplateViewDto create(Long userId, WeekTemplateCreateDto dto);
    WeekTemplateViewDto update(Long userId, Long id, WeekTemplateUpdateDto dto);
    WeekTemplateViewDto get(Long userId, Long id);
    void delete(Long userId, Long id);
}
