package com.example.activity_diary.dto.dictionary;

import com.example.activity_diary.entity.enums.Role;
import lombok.Data;

@Data
public class DictionaryUpdateDto {
    private String label;

    private Boolean active;

    private Role allowedRole;
}
