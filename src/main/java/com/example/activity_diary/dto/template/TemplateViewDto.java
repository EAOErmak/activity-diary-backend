package com.example.activity_diary.dto.template;

import com.example.activity_diary.entity.enums.TemplateType;
import lombok.Data;

import java.util.List;

@Data
public class TemplateViewDto {
    Long id;
    TemplateType type;
    String name;

    // Только одно из них будет заполнено в зависимости от type
    List<TemplateEntryItemDto> dayItems;
    List<TemplateDayItemDto> weekItems;

    List<TemplateGoalTagDto> goalTags;
    List<TemplateGoalMetricDto> goalMetrics;
}
