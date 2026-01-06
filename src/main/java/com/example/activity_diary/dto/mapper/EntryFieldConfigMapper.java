package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.diary.EntryFieldConfigDto;
import com.example.activity_diary.entity.EntryFieldConfig;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EntryFieldConfigMapper {
    EntryFieldConfigDto toDto(EntryFieldConfig entity);
    EntryFieldConfig toEntity(EntryFieldConfigDto dto);
}
