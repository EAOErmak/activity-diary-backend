// src/main/java/com/example/activity_diary/dto/mapper/DictionaryMapper.java
package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.entity.dict.DictionaryItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DictionaryMapper {

    // Для пользователя — имена полей совпадают (id, type, label, active, allowedRole, createdAt, updatedAt)
    @Mapping(source = "entryFieldConfig.id", target = "entryFieldConfigId")
    DictionaryResponseDto toDto(DictionaryItem item);
}
