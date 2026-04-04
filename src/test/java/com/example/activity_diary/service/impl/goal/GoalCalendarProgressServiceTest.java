package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.goal.DiaryEntryGoalDetailDto;
import com.example.activity_diary.dto.mapper.GoalMapper;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.goal.DayGoal;
import com.example.activity_diary.entity.goal.DiaryEntryGoal;
import com.example.activity_diary.entity.goal.WeekGoal;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.repository.goal.DayGoalRepository;
import com.example.activity_diary.repository.goal.DiaryEntryGoalRepository;
import com.example.activity_diary.repository.goal.WeekGoalRepository;
import com.example.activity_diary.service.diary.DiaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalCalendarProgressServiceTest {

    @Mock
    private DiaryEntryGoalRepository diaryEntryGoalRepository;

    @Mock
    private WeekGoalRepository weekGoalRepository;

    @Mock
    private DayGoalRepository dayGoalRepository;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private DiaryService diaryService;

    @Mock
    private GoalMapper goalMapper;

    @InjectMocks
    private GoalCalendarProgressService service;

    @Test
    void confirmEntryGoal_repeatsByUpdatingCurrentEntry() {
        Long userId = 10L;
        Long goalId = 20L;
        Long entryId = 30L;

        User user = userWithId(userId);
        WeekGoal week = weekGoal(user);
        DayGoal day = dayGoal(week);
        DiaryEntry currentEntry = entry(entryId, user, EntryStatus.FINISHED);
        DiaryEntryGoal goal = goal(goalId, user, day, currentEntry);
        day.setEntryGoals(List.of(goal));
        week.setDays(List.of(day));

        DiaryEntryCreateDto createDto = createDto();
        DiaryEntry updatedEntry = entry(entryId, user, EntryStatus.FINISHED);
        DiaryEntryDto updatedDto = new DiaryEntryDto();
        updatedDto.setId(entryId);

        DiaryEntryGoalDetailDto mapped = new DiaryEntryGoalDetailDto();

        when(diaryEntryGoalRepository.findByIdAndUser_Id(goalId, userId)).thenReturn(Optional.of(goal));
        when(diaryService.update(eq(entryId), any(DiaryEntryUpdateDto.class), eq(userId))).thenReturn(updatedDto);
        when(diaryRepository.findGraphByIdAndUser_Id(entryId, userId)).thenReturn(Optional.of(updatedEntry));
        when(goalMapper.toEntryView(goal)).thenReturn(mapped);

        DiaryEntryGoalDetailDto result = service.confirmEntryGoal(userId, goalId, createDto);

        assertEquals(mapped, result);
        assertEquals(updatedEntry, goal.getCurrentEntry());

        ArgumentCaptor<DiaryEntryUpdateDto> updateCaptor = ArgumentCaptor.forClass(DiaryEntryUpdateDto.class);
        verify(diaryService).update(eq(entryId), updateCaptor.capture(), eq(userId));
        DiaryEntryUpdateDto sentUpdate = updateCaptor.getValue();
        assertEquals(createDto.getWhenStarted(), sentUpdate.getWhenStarted());
        assertEquals(createDto.getWhenEnded(), sentUpdate.getWhenEnded());
        assertEquals(createDto.getMood(), sentUpdate.getMood());
        assertEquals(createDto.getDescription(), sentUpdate.getDescription());
        assertEquals(EntryStatus.FINISHED, sentUpdate.getStatus());
        assertEquals(1, sentUpdate.getMetrics().size());
        assertEquals(100L, sentUpdate.getMetrics().getFirst().getMetricTypeId());
        assertEquals(200L, sentUpdate.getMetrics().getFirst().getValues().getFirst().getUnitId());
        assertEquals(12, sentUpdate.getMetrics().getFirst().getValues().getFirst().getValue());

        verify(diaryService, never()).create(any(), eq(userId), any());
    }

    @Test
    void updateConfirmedEntryGoal_allowsPastDeadline() {
        Long userId = 10L;
        Long goalId = 20L;
        Long entryId = 30L;

        User user = userWithId(userId);
        WeekGoal week = weekGoal(user);
        DayGoal day = dayGoal(week);
        DiaryEntry currentEntry = entry(entryId, user, EntryStatus.FINISHED);
        DiaryEntryGoal goal = goal(goalId, user, day, currentEntry);
        day.setEntryGoals(List.of(goal));
        week.setDays(List.of(day));

        DiaryEntryUpdateDto updateDto = new DiaryEntryUpdateDto();
        updateDto.setDescription("updated #tag");
        DiaryEntryDto updatedDto = new DiaryEntryDto();
        updatedDto.setId(entryId);
        DiaryEntry updatedEntry = entry(entryId, user, EntryStatus.FINISHED);
        DiaryEntryGoalDetailDto mapped = new DiaryEntryGoalDetailDto();

        when(diaryEntryGoalRepository.findByIdAndUser_Id(goalId, userId)).thenReturn(Optional.of(goal));
        when(diaryService.update(entryId, updateDto, userId)).thenReturn(updatedDto);
        when(diaryRepository.findGraphByIdAndUser_Id(entryId, userId)).thenReturn(Optional.of(updatedEntry));
        when(goalMapper.toEntryView(goal)).thenReturn(mapped);

        DiaryEntryGoalDetailDto result = service.updateConfirmedEntryGoal(userId, goalId, updateDto);

        assertEquals(mapped, result);
        verify(diaryService).update(entryId, updateDto, userId);
    }

    private static DiaryEntryCreateDto createDto() {
        DiaryEntryCreateDto dto = new DiaryEntryCreateDto();
        dto.setWhenStarted(Instant.parse("2026-04-01T08:00:00Z"));
        dto.setWhenEnded(Instant.parse("2026-04-01T09:00:00Z"));
        dto.setMood((short) 4);
        dto.setDescription("updated #tag");

        var valueDto = new com.example.activity_diary.dto.diary.metric.EntryMetricValueCreateDto();
        valueDto.setUnitId(200L);
        valueDto.setValue(12);

        var metricDto = new com.example.activity_diary.dto.diary.metric.EntryMetricCreateDto();
        metricDto.setMetricTypeId(100L);
        metricDto.setValues(List.of(valueDto));

        dto.setMetrics(List.of(metricDto));
        return dto;
    }

    private static User userWithId(Long id) {
        User user = User.builder().username("user").build();
        user.setId(id);
        return user;
    }

    private static WeekGoal weekGoal(User user) {
        return WeekGoal.builder()
                .user(user)
                .whenStarted(Instant.parse("2026-03-31T00:00:00Z"))
                .whenEnded(Instant.parse("2026-04-06T00:00:00Z"))
                .build();
    }

    private static DayGoal dayGoal(WeekGoal week) {
        return DayGoal.builder()
                .weekGoal(week)
                .dayIndex(2)
                .targetDate(LocalDate.parse("2026-04-01"))
                .whenStarted(Instant.parse("2026-04-01T00:00:00Z"))
                .whenEnded(Instant.parse("2026-04-01T23:59:59Z"))
                .build();
    }

    private static DiaryEntryGoal goal(Long id, User user, DayGoal day, DiaryEntry currentEntry) {
        DiaryEntryGoal goal = DiaryEntryGoal.builder()
                .user(user)
                .dayGoal(day)
                .position(1)
                .whenStarted(Instant.parse("2026-03-01T08:00:00Z"))
                .whenEnded(Instant.parse("2026-03-01T09:00:00Z"))
                .expectedDurationMin(60)
                .name("Goal")
                .description("desc")
                .currentEntry(currentEntry)
                .build();
        goal.setId(id);
        return goal;
    }

    private static DiaryEntry entry(Long id, User user, EntryStatus status) {
        DiaryEntry entry = DiaryEntry.builder()
                .user(user)
                .whenStarted(Instant.parse("2026-03-01T08:00:00Z"))
                .whenEnded(Instant.parse("2026-03-01T09:00:00Z"))
                .duration(60)
                .description("desc #tag")
                .status(status)
                .build();
        entry.setId(id);
        return entry;
    }
}
