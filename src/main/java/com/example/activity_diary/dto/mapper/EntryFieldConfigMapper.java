package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.diary.EntryFieldConfigDto;
import com.example.activity_diary.entity.EntryFieldConfig;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public abstract class EntryFieldConfigMapper {

    public abstract EntryFieldConfigDto toDto(EntryFieldConfig entity);

    public abstract EntryFieldConfig toEntity(EntryFieldConfigDto dto);
}
