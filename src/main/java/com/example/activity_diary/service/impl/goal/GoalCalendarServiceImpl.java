package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.goal.DayGoalDetailDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalDetailDto;
import com.example.activity_diary.dto.goal.WeekGoalDetailDto;
import com.example.activity_diary.service.goal.GoalCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GoalCalendarServiceImpl implements GoalCalendarService {

    private final GoalCalendarCreateService goalCalendarCreateService;
    private final GoalCalendarProgressService goalCalendarProgressService;
    private final GoalCalendarMutationService goalCalendarMutationService;
    private final GoalDiaryEntryCommandFactory goalDiaryEntryCommandFactory;

    @Override
    public DiaryEntryGoalDetailDto createEntryGoal(Long userId, Long templateId, LocalDate targetDate) {
        return goalCalendarCreateService.createEntryGoal(userId, templateId, targetDate);
    }

    @Override
    public DayGoalDetailDto createDayGoal(Long userId, Long templateId, LocalDate targetDate) {
        return goalCalendarCreateService.createDayGoal(userId, templateId, targetDate);
    }

    @Override
    public WeekGoalDetailDto createWeekGoal(Long userId, Long templateId, LocalDate targetDate) {
        return goalCalendarCreateService.createWeekGoal(userId, templateId, targetDate);
    }

    @Override
    public DiaryEntryGoalDetailDto confirmEntryGoal(Long userId, Long goalId, DiaryEntryCreateDto dto) {
        return goalCalendarProgressService.confirmEntryGoal(
                userId,
                goalId,
                goalDiaryEntryCommandFactory.fromCreateDto(dto)
        );
    }

    @Override
    public DiaryEntryGoalDetailDto confirmEntryGoalSimple(Long userId, Long goalId) {
        return goalCalendarProgressService.confirmEntryGoalSimple(userId, goalId);
    }

    @Override
    public DiaryEntryGoalDetailDto updateConfirmedEntryGoal(Long userId, Long goalId, DiaryEntryUpdateDto dto) {
        return goalCalendarProgressService.updateConfirmedEntryGoal(
                userId,
                goalId,
                goalDiaryEntryCommandFactory.fromUpdateDto(dto)
        );
    }

    @Override
    public DayGoalDetailDto confirmDayGoal(Long userId, Long dayGoalId) {
        return goalCalendarProgressService.confirmDayGoal(userId, dayGoalId);
    }

    @Override
    public WeekGoalDetailDto replaceWeekGoal(Long userId, Long templateId, LocalDate targetDate) {
        return goalCalendarMutationService.replaceWeekGoal(userId, templateId, targetDate);
    }

    @Override
    public DayGoalDetailDto replaceDayGoal(Long userId, Long templateId, LocalDate targetDate) {
        return goalCalendarMutationService.replaceDayGoal(userId, templateId, targetDate);
    }

    @Override
    public void deleteWeekGoal(Long userId, LocalDate targetDate) {
        goalCalendarMutationService.deleteWeekGoal(userId, targetDate);
    }

    @Override
    public void deleteDayGoal(Long userId, LocalDate targetDate) {
        goalCalendarMutationService.deleteDayGoal(userId, targetDate);
    }

    @Override
    public void deleteEntryGoal(Long userId, Long entryGoalId) {
        goalCalendarMutationService.deleteEntryGoal(userId, entryGoalId);
    }
}
