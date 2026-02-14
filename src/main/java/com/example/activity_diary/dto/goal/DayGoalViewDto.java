package com.example.activity_diary.dto.goal;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class DayGoalViewDto {
    Long id;
    Integer dayIndex;
    LocalDate targetDate;
    Instant whenStarted;
    Instant whenEnded;
    Integer completeness;
    List<DiaryEntryGoalViewDto> entries;
}
