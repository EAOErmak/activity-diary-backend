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

    @Mapping(target = "currentEntryId",
            expression = "java(g.getCurrentEntry() == null ? null : g.getCurrentEntry().getId())")
    @Mapping(target = "metricGoals", source = "metricGoals")
    DiaryEntryGoalDetailDto toEntryDetail(DiaryEntryGoal g);

    List<DiaryEntryGoalSummaryDto> toEntrySummaryList(List<DiaryEntryGoal> goals);

    // =========================
    // MetricGoal -> DTO
    // =========================

    @Mapping(target = "metricTypeId", source = "metricType.id")
    @Mapping(target = "metricTypeName", source = "metricType.label") // С‚С‹ РіРѕРІРѕСЂРёР» label
    @Mapping(target = "values", source = "values")
    EntryMetricGoalDto toMetricGoalDto(EntryMetricGoal mg);

    List<EntryMetricGoalDto> toMetricGoalDtoList(List<EntryMetricGoal> mgs);

    // =========================
    // MetricValueGoal -> DTO
    // =========================

    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "unitName", source = "unit.label")
    // Р’РђР–РќРћ: РїРѕР»Рµ РІ DTO РґРѕР»Р¶РЅРѕ РЅР°Р·С‹РІР°С‚СЊСЃСЏ expectedValue (РєР°Рє Сѓ С‚РµР±СЏ) РёР»Рё РїСЂРѕРїРёС€Рё РїСЂР°РІРёР»СЊРЅРѕРµ РёРјСЏ
    @Mapping(target = "expectedValue", source = "expectedValue")
    EntryMetricValueGoalDto toMetricValueGoalDto(EntryMetricValueGoal vg);

    List<EntryMetricValueGoalDto> toMetricValueGoalDtoList(List<EntryMetricValueGoal> vgs);

    // =========================
    // Day
    // =========================

    DayGoalSummaryDto toDaySummary(DayGoal d);

    @Mapping(target = "entries", ignore = true)
    DayGoalDetailDto toDayDetailBase(DayGoal d);

    List<DayGoalSummaryDto> toDaySummaryList(List<DayGoal> days);

    // =========================
    // Week
    // =========================

    WeekGoalSummaryDto toWeekSummary(WeekGoal w);

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
