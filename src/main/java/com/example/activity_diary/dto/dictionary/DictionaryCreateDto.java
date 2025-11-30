// src/main/java/com/example/activity_diary/dto/dictionary/DictionaryCreateDto.java
package com.example.activity_diary.dto.dictionary;

import com.example.activity_diary.entity.enums.DictionaryType;
import lombok.Data;

@Data
public class DictionaryCreateDto {
    private DictionaryType type;
    private String label;
    private String allowedRole; // может быть null = доступ всем
}
