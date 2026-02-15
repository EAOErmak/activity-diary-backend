package com.example.activity_diary.dto.goal;

import lombok.Data;
import java.time.Instant;

@Data
public class DiaryEntryGoalSummaryDto {
    Long id;
    Integer completeness;
    Instant whenStarted;
    Instant whenEnded;
}
