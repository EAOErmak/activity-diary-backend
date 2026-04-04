package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricCreateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricUpdateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueCreateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueUpdateDto;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.goal.DiaryEntryGoal;
import com.example.activity_diary.entity.goal.EntryMetricGoal;
import com.example.activity_diary.entity.goal.EntryMetricValueGoal;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GoalDiaryEntryCommandFactory {

    public DiaryEntryCreateDto toCreateDto(DiaryEntryGoal goal) {
        DiaryEntryCreateDto dto = new DiaryEntryCreateDto();
        dto.setWhenStarted(goal.getWhenStarted());
        dto.setWhenEnded(goal.getWhenEnded());
        dto.setMood(goal.getMood());
        dto.setDescription(goal.getDescription());

        if (goal.getMetricGoals() != null && !goal.getMetricGoals().isEmpty()) {
            List<EntryMetricCreateDto> metricDtos = new ArrayList<>();

            for (EntryMetricGoal metricGoal : goal.getMetricGoals()) {
                EntryMetricCreateDto metricDto = new EntryMetricCreateDto();
                metricDto.setMetricTypeId(metricGoal.getMetricType().getId());

                List<EntryMetricValueCreateDto> valueDtos = new ArrayList<>();
                for (EntryMetricValueGoal valueGoal : metricGoal.getValues()) {
                    EntryMetricValueCreateDto valueDto = new EntryMetricValueCreateDto();
                    valueDto.setUnitId(valueGoal.getUnit().getId());
                    valueDto.setValue(valueGoal.getExpectedValue());
                    valueDtos.add(valueDto);
                }

                metricDto.setValues(valueDtos);
                metricDtos.add(metricDto);
            }

            dto.setMetrics(metricDtos);
        }

        return dto;
    }

    public DiaryEntryUpdateDto toUpdateDto(DiaryEntryCreateDto createDto) {
        DiaryEntryUpdateDto updateDto = new DiaryEntryUpdateDto();
        updateDto.setWhenStarted(createDto.getWhenStarted());
        updateDto.setWhenEnded(createDto.getWhenEnded());
        updateDto.setMood(createDto.getMood());
        updateDto.setDescription(createDto.getDescription());
        updateDto.setStatus(EntryStatus.FINISHED);
        updateDto.setMetrics(toMetricUpdateDtos(createDto.getMetrics()));
        return updateDto;
    }

    private List<EntryMetricUpdateDto> toMetricUpdateDtos(List<EntryMetricCreateDto> createDtos) {
        if (createDtos == null) {
            return null;
        }

        List<EntryMetricUpdateDto> updateDtos = new ArrayList<>(createDtos.size());
        for (EntryMetricCreateDto createDto : createDtos) {
            EntryMetricUpdateDto updateDto = new EntryMetricUpdateDto();
            updateDto.setMetricTypeId(createDto.getMetricTypeId());
            updateDto.setValues(toMetricValueUpdateDtos(createDto.getValues()));
            updateDtos.add(updateDto);
        }

        return updateDtos;
    }

    private List<EntryMetricValueUpdateDto> toMetricValueUpdateDtos(List<EntryMetricValueCreateDto> createDtos) {
        if (createDtos == null) {
            return null;
        }

        List<EntryMetricValueUpdateDto> updateDtos = new ArrayList<>(createDtos.size());
        for (EntryMetricValueCreateDto createDto : createDtos) {
            EntryMetricValueUpdateDto updateDto = new EntryMetricValueUpdateDto();
            updateDto.setUnitId(createDto.getUnitId());
            updateDto.setValue(createDto.getValue());
            updateDtos.add(updateDto);
        }

        return updateDtos;
    }
}
