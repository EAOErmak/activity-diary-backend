package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.goal.*;
import com.example.activity_diary.entity.goal.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoalMapper {

    @Mapping(target = "days", expression = "java(week.getDays().stream().map(this::toDay).toList())")
    WeekGoalViewDto toWeek(WeekGoal week);

    @Mapping(target = "entries", expression = "java(day.getEntryGoals().stream().map(this::toEntry).toList())")
    DayGoalViewDto toDay(DayGoal day);

    @Mapping(target = "currentEntryId", expression = "java(goal.getCurrentEntry() == null ? null : goal.getCurrentEntry().getId())")
    DiaryEntryGoalViewDto toEntry(DiaryEntryGoal goal);

    @Mapping(target = "days", source = "days")
    WeekGoalViewDto toWeekView(WeekGoal week);
    List<WeekGoalViewDto> toWeekViews(List<WeekGoal> weeks);

    @Mapping(target = "entries", source = "entryGoals")
    DayGoalViewDto toDayView(DayGoal day);
    List<DayGoalViewDto> toDayViews(List<DayGoal> days);



    @Mapping(target = "currentEntryId", source = "currentEntry.id")
    DiaryEntryGoalViewDto toEntryView(DiaryEntryGoal goal);
    List<DiaryEntryGoalViewDto> toEntryViews(List<DiaryEntryGoal> goals);
}
