package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.entity.dict.DictionaryItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DictionaryMapper {

    @Mapping(source = "entryFieldConfig.id", target = "entryFieldConfigId")
    @Mapping(source = "parent.id", target = "parentId")
    DictionaryResponseDto toDto(DictionaryItem item);
}
