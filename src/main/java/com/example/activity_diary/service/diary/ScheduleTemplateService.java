package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.template.*;
import com.example.activity_diary.dto.template.day.DayTemplateCreateDto;
import com.example.activity_diary.dto.template.week.WeekTemplateCreateDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ScheduleTemplateService {

    // CREATE
    TemplateViewDto createDayTemplate(Long userId, DayTemplateCreateDto dto);

    TemplateViewDto createWeekTemplate(Long userId, WeekTemplateCreateDto dto);

    // UPDATE ITEMS
    void updateDayTemplateItems(Long userId, Long templateId, TemplateItemsUpdateDto dto);

    void updateWeekTemplateItems(Long userId, Long templateId, TemplateItemsUpdateDto dto);

    // READ
    TemplateViewDto getTemplate(Long userId, Long templateId);

    Page<TemplateListItemDto> listTemplates(Long userId, Pageable pageable);

    // DELETE
    void deleteTemplate(Long userId, Long templateId);
}
