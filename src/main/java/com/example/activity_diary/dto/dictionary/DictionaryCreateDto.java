package com.example.activity_diary.dto.dictionary;

import lombok.Data;

@Data
public class DictionaryCreateDto {
    private String name;
    private Long parentId;  // only for WHAT, optional for others
}
