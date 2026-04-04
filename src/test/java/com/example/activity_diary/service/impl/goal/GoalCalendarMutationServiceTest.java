package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.dto.goal.DayGoalDetailDto;
import com.example.activity_diary.dto.goal.WeekGoalDetailDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.goal.DayGoal;
import com.example.activity_diary.entity.goal.DiaryEntryGoal;
import com.example.activity_diary.entity.goal.WeekGoal;
import com.example.activity_diary.repository.goal.DayGoalRepository;
import com.example.activity_diary.repository.goal.DiaryEntryGoalRepository;
import com.example.activity_diary.repository.goal.WeekGoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalCalendarMutationServiceTest {

    @Mock
    private WeekGoalRepository weekGoalRepository;

    @Mock
    private DayGoalRepository dayGoalRepository;

    @Mock
    private DiaryEntryGoalRepository diaryEntryGoalRepository;

    @Mock
    private GoalCalendarCreateService goalCalendarCreateService;

    @InjectMocks
    private GoalCalendarMutationService service;

    @Test
    void deleteEntryGoal_allowsDeletingStartedGoalAndCleansEmptyParents() {
        Long userId = 10L;
        Long goalId = 30L;

        User user = userWithId(userId);
        WeekGoal week = weekGoal(user);
        DayGoal day = dayGoal(week);
        DiaryEntryGoal goal = DiaryEntryGoal.builder()
                .user(user)
                .dayGoal(day)
                .position(1)
                .whenStarted(Instant.parse("2026-03-01T08:00:00Z"))
                .whenEnded(Instant.parse("2026-03-01T09:00:00Z"))
                .expectedDurationMin(60)
                .name("Goal")
                .build();
        goal.setId(goalId);

        when(diaryEntryGoalRepository.findByIdAndUser_Id(goalId, userId)).thenReturn(Optional.of(goal));
        when(diaryEntryGoalRepository.countByDayGoal_Id(day.getId())).thenReturn(0L);
        when(dayGoalRepository.countByWeekGoal_Id(week.getId())).thenReturn(0L);

        service.deleteEntryGoal(userId, goalId);

        verify(diaryEntryGoalRepository).delete(goal);
        verify(dayGoalRepository).delete(day);
        verify(weekGoalRepository).delete(week);
    }

    @Test
    void replaceWeekGoal_allowsReplacingStartedWeek() {
        Long userId = 10L;
        Long templateId = 50L;
        LocalDate targetDate = LocalDate.parse("2026-04-01");

        User user = userWithId(userId);
        WeekGoal existingWeek = weekGoal(user);
        WeekGoalDetailDto created = new WeekGoalDetailDto();

        when(weekGoalRepository.findByUser_IdAndWhenStarted(userId, targetDate.with(java.time.DayOfWeek.MONDAY)
                .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())).thenReturn(Optional.of(existingWeek));
        when(goalCalendarCreateService.createWeekGoal(userId, templateId, targetDate)).thenReturn(created);

        WeekGoalDetailDto result = service.replaceWeekGoal(userId, templateId, targetDate);

        assertEquals(created, result);
        verify(weekGoalRepository).delete(existingWeek);
        verify(goalCalendarCreateService).createWeekGoal(userId, templateId, targetDate);
    }

    @Test
    void replaceDayGoal_allowsReplacingStartedDay() {
        Long userId = 10L;
        Long templateId = 60L;
        LocalDate targetDate = LocalDate.parse("2026-04-01");

        User user = userWithId(userId);
        WeekGoal week = weekGoal(user);
        DayGoal existingDay = dayGoal(week);
        DayGoalDetailDto created = new DayGoalDetailDto();

        when(weekGoalRepository.findByUser_IdAndWhenStarted(userId, targetDate.with(java.time.DayOfWeek.MONDAY)
                .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())).thenReturn(Optional.of(week));
        when(dayGoalRepository.findByWeekGoal_IdAndTargetDate(week.getId(), targetDate)).thenReturn(Optional.of(existingDay));
        when(dayGoalRepository.countByWeekGoal_Id(week.getId())).thenReturn(1L);
        when(goalCalendarCreateService.createDayGoal(userId, templateId, targetDate)).thenReturn(created);

        DayGoalDetailDto result = service.replaceDayGoal(userId, templateId, targetDate);

        assertEquals(created, result);
        verify(dayGoalRepository).delete(existingDay);
        verify(weekGoalRepository, never()).delete(week);
        verify(goalCalendarCreateService).createDayGoal(userId, templateId, targetDate);
    }

    private static User userWithId(Long id) {
        User user = User.builder().username("user").build();
        user.setId(id);
        return user;
    }

    private static WeekGoal weekGoal(User user) {
        WeekGoal week = WeekGoal.builder()
                .user(user)
                .whenStarted(Instant.parse("2026-03-31T00:00:00Z"))
                .whenEnded(Instant.parse("2026-04-06T00:00:00Z"))
                .build();
        week.setId(11L);
        return week;
    }

    private static DayGoal dayGoal(WeekGoal week) {
        DayGoal day = DayGoal.builder()
                .weekGoal(week)
                .dayIndex(3)
                .targetDate(LocalDate.parse("2026-04-01"))
                .whenStarted(Instant.parse("2026-04-01T00:00:00Z"))
                .whenEnded(Instant.parse("2026-04-01T23:59:59Z"))
                .build();
        day.setId(22L);
        return day;
    }
}
