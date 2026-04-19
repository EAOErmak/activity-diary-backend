package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.template.day.DayTemplateViewDto;
import com.example.activity_diary.dto.template.day.TemplateEntryItemViewDto;
import com.example.activity_diary.entity.template.DayTemplate;
import com.example.activity_diary.entity.template.TemplateEntryItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DayTemplateMapper {
    @Mapping(target = "items", source = "items")
    DayTemplateViewDto toView(DayTemplate tpl);

    @Mapping(target = "entryTemplateId", source = "entryTemplate.id")
    @Mapping(target = "entryTemplateName", source = "entryTemplate.name")
    TemplateEntryItemViewDto toViewItem(TemplateEntryItem item);
}
