package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.goal.*;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.goal.EntryMetricGoal;
import com.example.activity_diary.entity.goal.EntryMetricValueGoal;
import com.example.activity_diary.entity.goal.DiaryEntryGoal;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DiaryEntryGoalMapper {

    @Mapping(target = "currentEntryId",
            expression = "java(goal.getCurrentEntry() == null ? null : goal.getCurrentEntry().getId())")
    @Mapping(target = "metricGoals", source = "metricGoals")
    DiaryEntryGoalDetailDto toDetailDto(DiaryEntryGoal goal);

    List<DiaryEntryGoalDetailDto> toDetailDtos(List<DiaryEntryGoal> goals);

    // ---------- MetricGoal ----------

    @Mapping(target = "metricTypeId", source = "metricType.id")
    @Mapping(target = "metricTypeName", source = "metricType", qualifiedByName = "dictName")
    @Mapping(target = "values", source = "values")
    EntryMetricGoalDto toMetricGoalDto(EntryMetricGoal mg);

    List<EntryMetricGoalDto> toMetricGoalDtos(List<EntryMetricGoal> mgs);

    // ---------- MetricValueGoal ----------

    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "unitName", source = "unit", qualifiedByName = "dictName")
    EntryMetricValueGoalDto toMetricValueGoalDto(EntryMetricValueGoal vg);

    List<EntryMetricValueGoalDto> toMetricValueGoalDtos(List<EntryMetricValueGoal> vgs);

    // ---------- helpers ----------

    @Named("dictName")
    default String dictName(DictionaryItem item) {
        return item == null ? null : item.getLabel(); // РїРѕРјРµРЅСЏР№, РµСЃР»Рё РїРѕР»Рµ РЅРµ name
    }
}
