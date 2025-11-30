// src/main/java/com/example/activity_diary/dto/dictionary/DictionaryUpdateDto.java
package com.example.activity_diary.dto.dictionary;

import lombok.Data;

@Data
public class DictionaryUpdateDto {
    private String label;       // null = не менять

    private Boolean active;     // null = не менять

    private String allowedRole; // null = не менять
}
