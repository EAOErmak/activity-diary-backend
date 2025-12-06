package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.diary.EntryFieldConfigDto;
import com.example.activity_diary.entity.EntryFieldConfig;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public abstract class EntryFieldConfigMapper {

    @Mapping(source = "whatHappened.id", target = "whatHappenedId")
    public abstract EntryFieldConfigDto toDto(EntryFieldConfig entity);

    @Mapping(target = "whatHappened", ignore = true)
    public abstract EntryFieldConfig toEntity(EntryFieldConfigDto dto);
}
