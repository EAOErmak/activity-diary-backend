package com.example.activity_diary.dto.diary;

import lombok.Data;

@Data
public class EntryFieldConfigDto {
    private Long id;
    private String name;

    private Long whatHappenedId; // nullable

    private Boolean showWhat;
    private Boolean showActivities;
    private Boolean showFeeling;
    private Boolean showDescription;

    private Boolean requiredWhat;
    private Boolean requiredActivities;
}
