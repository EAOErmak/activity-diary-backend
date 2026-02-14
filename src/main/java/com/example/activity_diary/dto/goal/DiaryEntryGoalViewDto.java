package com.example.activity_diary.dto.goal;

import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
public class DiaryEntryGoalViewDto {
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
