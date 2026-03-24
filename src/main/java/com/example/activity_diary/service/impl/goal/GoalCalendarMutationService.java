package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.dto.goal.DayGoalDetailDto;
import com.example.activity_diary.dto.goal.WeekGoalDetailDto;
import com.example.activity_diary.entity.goal.DayGoal;
import com.example.activity_diary.entity.goal.DiaryEntryGoal;
import com.example.activity_diary.entity.goal.WeekGoal;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.goal.DayGoalRepository;
import com.example.activity_diary.repository.goal.DiaryEntryGoalRepository;
import com.example.activity_diary.repository.goal.WeekGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalCalendarMutationService {

    private final WeekGoalRepository weekGoalRepository;
    private final DayGoalRepository dayGoalRepository;
    private final DiaryEntryGoalRepository diaryEntryGoalRepository;
    private final GoalCalendarCreateService goalCalendarCreateService;

    public void deleteWeekGoal(Long userId, LocalDate targetDate) {
        WeekGoal week = findWeekGoalByDate(userId, targetDate);
        assertNotStarted(week.getWhenStarted(), "Week already started");
        weekGoalRepository.delete(week);
    }

    public WeekGoalDetailDto replaceWeekGoal(Long userId, Long templateId, LocalDate targetDate) {
        Instant weekStart = weekStartInstant(targetDate);

        weekGoalRepository.findByUser_IdAndWhenStarted(userId, weekStart).ifPresent(week -> {
            assertNotStarted(week.getWhenStarted(), "Week already started");
            weekGoalRepository.delete(week);
        });

        return goalCalendarCreateService.createWeekGoal(userId, templateId, targetDate);
    }

    public void deleteDayGoal(Long userId, LocalDate targetDate) {
        WeekGoal week = findWeekGoalByDate(userId, targetDate);

        DayGoal day = dayGoalRepository.findByWeekGoal_IdAndTargetDate(week.getId(), targetDate)
                .orElseThrow(() -> new NotFoundException("DayGoal not found"));

        assertNotStarted(day.getWhenStarted(), "Day already started");
        dayGoalRepository.delete(day);

        deleteWeekIfEmpty(week);
    }

    public DayGoalDetailDto replaceDayGoal(Long userId, Long templateId, LocalDate targetDate) {
        WeekGoal week = findWeekGoalByDate(userId, targetDate);

        dayGoalRepository.findByWeekGoal_IdAndTargetDate(week.getId(), targetDate).ifPresent(day -> {
            assertNotStarted(day.getWhenStarted(), "Day already started");
            dayGoalRepository.delete(day);
            deleteWeekIfEmpty(week);
        });

        return goalCalendarCreateService.createDayGoal(userId, templateId, targetDate);
    }

    public void deleteEntryGoal(Long userId, Long entryGoalId) {
        DiaryEntryGoal goal = diaryEntryGoalRepository.findByIdAndUser_Id(entryGoalId, userId)
                .orElseThrow(() -> new NotFoundException("DiaryEntryGoal not found"));

        assertNotStarted(goal.getWhenStarted(), "EntryGoal already started");

        DayGoal day = goal.getDayGoal();
        WeekGoal week = day.getWeekGoal();

        diaryEntryGoalRepository.delete(goal);

        if (diaryEntryGoalRepository.countByDayGoal_Id(day.getId()) == 0) {
            assertNotStarted(day.getWhenStarted(), "Day already started");
            dayGoalRepository.delete(day);
            deleteWeekIfEmpty(week);
        }
    }

    private WeekGoal findWeekGoalByDate(Long userId, LocalDate targetDate) {
        return weekGoalRepository.findByUser_IdAndWhenStarted(userId, weekStartInstant(targetDate))
                .orElseThrow(() -> new NotFoundException("WeekGoal not found"));
    }

    private void deleteWeekIfEmpty(WeekGoal week) {
        if (dayGoalRepository.countByWeekGoal_Id(week.getId()) == 0) {
            assertNotStarted(week.getWhenStarted(), "Week already started");
            weekGoalRepository.delete(week);
        }
    }

    private Instant weekStartInstant(LocalDate anyDateInWeek) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate monday = anyDateInWeek.with(DayOfWeek.MONDAY);
        return monday.atStartOfDay(zone).toInstant();
    }

    private void assertNotStarted(Instant whenStarted, String message) {
        if (!Instant.now().isBefore(whenStarted)) {
            throw new BadRequestException(message);
        }
    }
}
