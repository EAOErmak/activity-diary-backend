package com.example.activity_diary.dto.goal;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DayGoalSummaryDto {
    Long id;
    LocalDate targetDate;
    Integer completeness;
}
