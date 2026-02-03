package com.example.activity_diary.dto.diary;

import lombok.Data;

@Data
public class EntryFieldConfigDto {

    private Long id;
    private String name;

    private Boolean showMetrics;
    private Boolean showMood;
    private Boolean showDescription;

    private Boolean requiredSubCategory;
    private Boolean requiredMetrics;
}
