package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.template.*;
import com.example.activity_diary.entity.DiaryEntryTemplate;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {TagMapper.class, EntryTemplateMetricMapper.class})
public interface DiaryEntryTemplateMapper {

    // View
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "metrics", source = "metrics")
    DiaryEntryTemplateViewDto toViewDto(DiaryEntryTemplate entity);

    // Если тебе нужен "листинг" без metrics — можно сделать отдельный метод
    @Mapping(target = "metrics", ignore = true)
    DiaryEntryTemplateViewDto toListItemDto(DiaryEntryTemplate entity);

    // PATCH update: обновлять ТОЛЬКО не-null поля (name/description/mood)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchBasics(@MappingTarget DiaryEntryTemplate entity, DiaryEntryTemplateUpdateDto dto);
}
