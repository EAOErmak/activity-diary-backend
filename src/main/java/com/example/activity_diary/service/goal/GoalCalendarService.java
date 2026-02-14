package com.example.activity_diary.service.goal;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.goal.DayGoalViewDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalViewDto;
import com.example.activity_diary.dto.goal.WeekGoalViewDto;

import java.time.LocalDate;

public interface GoalCalendarService {

    DiaryEntryGoalViewDto createEntryGoal(Long userId, Long templateId, LocalDate targetDate);

    DayGoalViewDto createDayGoal(Long userId, Long templateId, LocalDate targetDate);

    WeekGoalViewDto createWeekGoal(Long userId, Long templateId, LocalDate targetDate);

    DiaryEntryGoalViewDto confirmEntryGoal(Long userId, Long goalId, DiaryEntryCreateDto dto);

    DiaryEntryGoalViewDto updateConfirmedEntryGoal(Long userId, Long goalId, DiaryEntryUpdateDto dto);

    DayGoalViewDto confirmDayGoal(Long userId, Long dayGoalId);
}
