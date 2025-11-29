package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.entity.dict.ActivityItemNameDict;
import com.example.activity_diary.entity.dict.ActivityItemUnitDict;
import com.example.activity_diary.entity.dict.WhatDict;
import com.example.activity_diary.entity.dict.WhatHappenedDict;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DictionaryMapper {

    // WHAT HAPPENED
    @Mapping(target = "parentId", ignore = true)
    DictionaryResponseDto toDto(WhatHappenedDict d);

    // WHAT
    @Mapping(source = "whatHappenedId", target = "parentId")
    DictionaryResponseDto toDto(WhatDict d);


    // ITEM NAME
    @Mapping(target = "parentId", ignore = true)
    DictionaryResponseDto toDto(ActivityItemNameDict d);

    // UNIT
    @Mapping(target = "parentId", ignore = true)
    DictionaryResponseDto toDto(ActivityItemUnitDict d);

}
