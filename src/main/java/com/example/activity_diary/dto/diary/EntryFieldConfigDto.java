package com.example.activity_diary.dto.diary;

import lombok.Data;

@Data
public class EntryFieldConfigDto {

    private Long id;
    private String name;

    // ✅ Категория (было: whatHappenedId)
    private Long categoryId; // nullable

    // ✅ Что показывать
    private Boolean showSubCategory;   // было: showWhat
    private Boolean showMetrics;       // было: showActivities
    private Boolean showMood;          // было: showFeeling
    private Boolean showDescription;  // ✅ оставили как есть

    // ✅ Что обязательно
    private Boolean requiredSubCategory; // было: requiredWhat
    private Boolean requiredMetrics;     // было: requiredActivities
}
