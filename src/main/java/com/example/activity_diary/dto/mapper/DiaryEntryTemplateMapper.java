package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateUpdateDto;
import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateViewDto;
import com.example.activity_diary.entity.template.DiaryEntryTemplate;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {EntryTemplateMetricMapper.class})
public interface DiaryEntryTemplateMapper {

    // View
    @Mapping(target = "metrics", source = "metrics")
    DiaryEntryTemplateViewDto toViewDto(DiaryEntryTemplate entity);

    // List item без metrics
    @Mapping(target = "metrics", ignore = true)
    DiaryEntryTemplateViewDto toListItemDto(DiaryEntryTemplate entity);

    // PATCH update: обновлять только не-null поля (включая timeStart/timeEnd)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "metrics", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void patchBasics(@MappingTarget DiaryEntryTemplate entity, DiaryEntryTemplateUpdateDto dto);
}
