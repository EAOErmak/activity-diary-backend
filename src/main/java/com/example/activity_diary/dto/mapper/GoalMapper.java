package com.example.activity_diary.dto.mapper;
import com.example.activity_diary.dto.goal.*;
import com.example.activity_diary.entity.goal.*;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface GoalMapper {

    @Mapping(target = "days", source = "days")
    WeekGoalDetailDto toWeekView(WeekGoal week);

    List<WeekGoalDetailDto> toWeekViews(List<WeekGoal> weeks);

    @Mapping(target = "entries", source = "entryGoals")
    DayGoalDetailDto toDayView(DayGoal day);

    List<DayGoalDetailDto> toDayViews(List<DayGoal> days);

    // ✅ Добавили metricGoals
    @Mapping(target = "currentEntryId", source = "currentEntry.id")
    @Mapping(target = "metricGoals", source = "metricGoals")
    DiaryEntryGoalDetailDto toEntryView(DiaryEntryGoal goal);

    List<DiaryEntryGoalDetailDto> toEntryViews(List<DiaryEntryGoal> goals);

    // -------- metric goal mapping --------

    @Mapping(target = "metricTypeId", source = "metricType.id")
    @Mapping(target = "metricTypeName", source = "metricType.label")
    @Mapping(target = "values", source = "values")
    EntryMetricGoalDto toMetricGoalDto(EntryMetricGoal mg);

    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "unitName", source = "unit.label")
    @Mapping(target = "expectedValue", source = "expectedValue") // или target="value" если так в DTO
    EntryMetricValueGoalDto toMetricValueGoalDto(EntryMetricValueGoal vg);
}
