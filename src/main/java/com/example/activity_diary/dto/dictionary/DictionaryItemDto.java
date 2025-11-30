// src/main/java/com/example/activity_diary/dto/dictionary/DictionaryItemDto.java
package com.example.activity_diary.dto.dictionary;

import com.example.activity_diary.entity.enums.DictionaryType;
import lombok.Data;

@Data
public class DictionaryItemDto {
    private Long id;
    private DictionaryType type;
    private String label;
    private boolean active;
    private String allowedRole;
}
