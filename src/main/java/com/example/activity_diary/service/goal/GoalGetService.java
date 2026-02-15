package com.example.activity_diary.service.goal;

import com.example.activity_diary.dto.goal.*;

import java.time.LocalDate;
import java.util.List;

public interface GoalGetService {
    DiaryEntryGoalSummaryDto getEntryGoalSummary(Long userId, Long goalId);
    DiaryEntryGoalDetailDto getEntryGoalDetail(Long userId, Long goalId);

    DayGoalSummaryDto getDayGoalSummary(Long userId, Long dayGoalId);
    DayGoalDetailDto getDayGoalDetail(Long userId, Long dayGoalId);

    WeekGoalSummaryDto getWeekGoalSummary(Long userId, Long weekGoalId);
    WeekGoalDetailDto getWeekGoalDetail(Long userId, Long weekGoalId);

    List<WeekGoalSummaryDto> listWeekSummaries(Long userId, LocalDate from, LocalDate to);
    List<DayGoalSummaryDto> listDaySummaries(Long userId, LocalDate from, LocalDate to);
    List<DiaryEntryGoalSummaryDto> listEntrySummariesByDate(Long userId, LocalDate date);
    List<DiaryEntryGoalSummaryDto> listEntrySummariesByDayGoal(Long userId, Long dayGoalId);
}
