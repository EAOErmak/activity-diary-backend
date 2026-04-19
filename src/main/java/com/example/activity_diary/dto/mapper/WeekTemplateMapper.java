package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.template.week.TemplateDayItemViewDto;
import com.example.activity_diary.dto.template.week.WeekTemplateViewDto;
import com.example.activity_diary.entity.template.TemplateDayItem;
import com.example.activity_diary.entity.template.WeekTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WeekTemplateMapper {
    WeekTemplateViewDto toView(WeekTemplate tpl);

    @Mapping(target = "dayTemplateId", source = "dayTemplate.id")
    @Mapping(target = "dayTemplateName", source = "dayTemplate.name")
    TemplateDayItemViewDto toViewItem(TemplateDayItem item);
}
