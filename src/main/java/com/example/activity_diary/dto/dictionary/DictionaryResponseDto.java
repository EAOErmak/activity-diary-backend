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

    private Long parentId;

    private boolean active;

    private String allowedRole;

    private ChartType chartType;

    private Long entryFieldConfigId;
}
