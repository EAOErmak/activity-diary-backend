package com.example.activity_diary.dto.goal;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class WeekGoalDetailDto {
    Long id;
    Instant whenStarted;
    Instant whenEnded;
    Integer completeness;
    List<DayGoalDetailDto> days;
}
