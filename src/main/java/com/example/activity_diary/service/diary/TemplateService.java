package com.example.activity_diary.service.diary;

import com.example.activity_diary.entity.Template;
import com.example.activity_diary.entity.User;

import java.util.List;

public interface TemplateService {
    Template createDayTemplate(User user, String name, List<Long> entryTemplateIds);
    Template createWeekTemplate(User user, String name, List<Long> dayTemplateIds);
    void updateDayTemplateItems(Long templateId, Long userId, List<Long> entryTemplateIds);
    void updateWeekTemplateItems(Long templateId, Long userId, List<Long> dayTemplateIds);
}
