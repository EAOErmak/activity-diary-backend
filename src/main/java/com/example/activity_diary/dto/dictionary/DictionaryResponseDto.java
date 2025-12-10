// src/main/java/com/example/activity_diary/dto/dictionary/DictionaryResponseDto.java
package com.example.activity_diary.dto.dictionary;

import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.entity.enums.DictionaryType;
import lombok.Data;

import java.time.Instant;

@Data
public class DictionaryResponseDto {
    private Long id;

    private DictionaryType type;

    private String label;

    private boolean active;

    private String allowedRole;

    private ChartType chartType;

    private Long entryFieldConfigId;


    private Instant createdAt;

    private Instant updatedAt;
}
