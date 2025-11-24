package com.example.activity_diary.dto.dictionary;

import lombok.Data;

@Data
public class DictionaryUpdateDto {
    private String name;     // null = не менять
    private Boolean isActive; // null = не менять
}
