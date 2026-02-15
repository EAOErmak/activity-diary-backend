package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.goal.*;
import com.example.activity_diary.entity.goal.*;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface GoalGetMapper {

    // =========================
    // Entry
    // =========================

    DiaryEntryGoalSummaryDto toEntrySummary(DiaryEntryGoal g);

    @Mapping(target = "currentEntryId", expression = "java(g.getCurrentEntry() == null ? null : g.getCurrentEntry().getId())")
    DiaryEntryGoalDetailDto toEntryDetail(DiaryEntryGoal g);

    List<DiaryEntryGoalSummaryDto> toEntrySummaryList(List<DiaryEntryGoal> goals);

    // =========================
    // Day
    // =========================

    DayGoalSummaryDto toDaySummary(DayGoal d);

    // ВАЖНО: entries мы в аннотациях не маппим, сделаем в default методе с сортировкой
    @Mapping(target = "entries", ignore = true)
    DayGoalDetailDto toDayDetailBase(DayGoal d);

    List<DayGoalSummaryDto> toDaySummaryList(List<DayGoal> days);

    // =========================
    // Week
    // =========================

    WeekGoalSummaryDto toWeekSummary(WeekGoal w);

    // ВАЖНО: days мы в аннотациях не маппим, сделаем в default методе с сортировкой
    @Mapping(target = "days", ignore = true)
    WeekGoalDetailDto toWeekDetailBase(WeekGoal w);

    List<WeekGoalSummaryDto> toWeekSummaryList(List<WeekGoal> weeks);

    // =========================
    // Default detail methods with sorting
    // =========================

    default DayGoalDetailDto toDayDetail(DayGoal d) {
        DayGoalDetailDto dto = toDayDetailBase(d);

        List<DiaryEntryGoalDetailDto> entries = d.getEntryGoals().stream()
                .sorted(Comparator.comparingInt(DiaryEntryGoal::getPosition))
                .map(this::toEntryDetail)
                .toList();

        dto.setEntries(entries);
        return dto;
    }

    default WeekGoalDetailDto toWeekDetail(WeekGoal w) {
        WeekGoalDetailDto dto = toWeekDetailBase(w);

        List<DayGoalDetailDto> days = w.getDays().stream()
                .sorted(Comparator.comparingInt(DayGoal::getDayIndex))
                .map(this::toDayDetail)
                .toList();

        dto.setDays(days);
        return dto;
    }
}
