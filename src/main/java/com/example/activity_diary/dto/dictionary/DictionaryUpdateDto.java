// src/main/java/com/example/activity_diary/dto/dictionary/DictionaryUpdateDto.java
package com.example.activity_diary.dto.dictionary;

import com.example.activity_diary.entity.enums.Role;
import lombok.Data;

@Data
public class DictionaryUpdateDto {
    private String label;       // null = не менять

    private Boolean active;     // null = не менять

    private Role allowedRole; // null = не менять
}
