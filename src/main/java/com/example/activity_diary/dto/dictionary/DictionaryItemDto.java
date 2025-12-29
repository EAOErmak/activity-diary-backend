package com.example.activity_diary.dto.dictionary;

import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.Role;
import lombok.Data;

@Data
public class DictionaryItemDto {
    private Long id;
    private DictionaryType type;
    private String label;
    private boolean active;
    private Role allowedRole;
}
