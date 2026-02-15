package com.example.activity_diary.dto.goal;

import lombok.Data;

import java.time.Instant;

@Data
public class DiaryEntryGoalDetailDto {
    Long id;
    Integer position;
    Instant whenStarted;
    Instant whenEnded;
    Integer expectedDurationMin;

    String name;
    Short mood;
    String description;

    Integer completeness;

    Long currentEntryId;
}
