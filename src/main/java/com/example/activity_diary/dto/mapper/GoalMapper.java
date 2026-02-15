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

    @Mapping(target = "currentEntryId", source = "currentEntry.id")
    DiaryEntryGoalDetailDto toEntryView(DiaryEntryGoal goal);

    List<DiaryEntryGoalDetailDto> toEntryViews(List<DiaryEntryGoal> goals);
}