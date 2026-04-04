package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.dictionary.DictionaryOptionDto;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.entity.dict.DictionaryItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DictionaryMapper {

    @Mapping(source = "entryFieldConfig.id", target = "entryFieldConfigId")
    DictionaryResponseDto toDto(DictionaryItem item);

    DictionaryOptionDto toOptionDto(DictionaryItem item);
}
