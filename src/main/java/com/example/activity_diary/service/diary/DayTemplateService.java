package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.template.day.DayTemplateCreateDto;
import com.example.activity_diary.dto.template.day.DayTemplateUpdateDto;
import com.example.activity_diary.dto.template.day.DayTemplateViewDto;

public interface DayTemplateService {
    DayTemplateViewDto create(Long userId, DayTemplateCreateDto dto);
    DayTemplateViewDto update(Long userId, Long id, DayTemplateUpdateDto dto);
    DayTemplateViewDto get(Long userId, Long id);
    void delete(Long userId, Long id);
}
