package com.example.activity_diary.dto.goal;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DayGoalCreateDto {
    Long templateId;
    LocalDate targetDate;
}
