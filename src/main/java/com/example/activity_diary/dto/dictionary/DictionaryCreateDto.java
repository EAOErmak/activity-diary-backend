// src/main/java/com/example/activity_diary/dto/dictionary/DictionaryCreateDto.java
package com.example.activity_diary.dto.dictionary;

import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.Role;
import lombok.Data;

@Data
public class DictionaryCreateDto {
    private DictionaryType type;
    private String label;
    private Role allowedRole; // может быть null = доступ всем
    private Long parentId;
    private ChartType chartType;
    private Long entryFieldConfigId;
}
