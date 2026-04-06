package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.dictionary.DictionaryOptionDto;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.entity.dict.DictionaryItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DictionaryMapper {

    DictionaryResponseDto toDto(DictionaryItem item);

    DictionaryOptionDto toOptionDto(DictionaryItem item);
}
