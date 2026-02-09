package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.template.*;
import com.example.activity_diary.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ScheduleTemplateMapper {

    // Template -> ViewDto (без вложенных коллекций автоматически)
    @Mapping(target = "dayItems", ignore = true)
    @Mapping(target = "weekItems", ignore = true)
    @Mapping(target = "goalTags", ignore = true)
    @Mapping(target = "goalMetrics", ignore = true)
    TemplateViewDto toViewDto(Template t);

    // DAY item
    @Mapping(target = "entryTemplateId", source = "entryTemplate.id")
    @Mapping(target = "entryTemplateName", source = "entryTemplate.name")
    TemplateEntryItemDto toDto(TemplateEntryItem item);

    // WEEK item
    @Mapping(target = "dayTemplateId", source = "dayTemplate.id")
    @Mapping(target = "dayTemplateName", source = "dayTemplate.name")
    TemplateDayItemDto toDto(TemplateDayItem item);

    // Goals
    @Mapping(target = "tagId", source = "tag.id")
    @Mapping(target = "tagName", source = "tag.name")
    TemplateGoalTagDto toDto(TemplateGoalTag g);

    @Mapping(target = "metricTypeId", source = "metricType.id")
    @Mapping(target = "unitId", source = "unit.id")
    TemplateGoalMetricDto toDto(TemplateGoalMetric g);
}
