package com.example.activity_diary.service.goal;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.goal.DayGoalDetailDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalDetailDto;
import com.example.activity_diary.dto.goal.WeekGoalDetailDto;

import java.time.LocalDate;

public interface GoalCalendarService {

    DiaryEntryGoalDetailDto createEntryGoal(Long userId, Long templateId, LocalDate targetDate);

    DayGoalDetailDto createDayGoal(Long userId, Long templateId, LocalDate targetDate);

    WeekGoalDetailDto createWeekGoal(Long userId, Long templateId, LocalDate targetDate);

    DiaryEntryGoalDetailDto confirmEntryGoal(Long userId, Long goalId, DiaryEntryCreateDto dto);
    DiaryEntryGoalDetailDto confirmEntryGoalSimple(Long userId, Long goalId);

    DiaryEntryGoalDetailDto updateConfirmedEntryGoal(Long userId, Long goalId, DiaryEntryUpdateDto dto);

    DayGoalDetailDto confirmDayGoal(Long userId, Long dayGoalId);

    WeekGoalDetailDto replaceWeekGoal(Long userId, Long templateId, LocalDate targetDate);
    DayGoalDetailDto replaceDayGoal(Long userId, Long templateId, LocalDate targetDate);

    void deleteWeekGoal(Long userId, LocalDate targetDate);
    void deleteDayGoal(Long userId, LocalDate targetDate);
    void deleteEntryGoal(Long userId, Long entryGoalId);
}
