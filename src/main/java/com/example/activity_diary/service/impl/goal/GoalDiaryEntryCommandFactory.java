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
import java.util.Collection;
import java.util.List;

@Component
public class GoalDiaryEntryCommandFactory {

    public GoalDiaryEntryCommand fromGoal(DiaryEntryGoal goal) {
        return new GoalDiaryEntryCommand(
                goal.getWhenStarted(),
                goal.getWhenEnded(),
                goal.getMood(),
                goal.getDescription(),
                null,
                toMetricCommands(goal.getMetricGoals())
        );
    }

    public GoalDiaryEntryCommand fromCreateDto(DiaryEntryCreateDto dto) {
        return new GoalDiaryEntryCommand(
                dto.getWhenStarted(),
                dto.getWhenEnded(),
                dto.getMood(),
                dto.getDescription(),
                null,
                toMetricCommandsFromCreate(dto.getMetrics())
        );
    }

    public GoalDiaryEntryCommand fromUpdateDto(DiaryEntryUpdateDto dto) {
        return new GoalDiaryEntryCommand(
                dto.getWhenStarted(),
                dto.getWhenEnded(),
                dto.getMood(),
                dto.getDescription(),
                dto.getStatus(),
                toMetricCommandsFromUpdate(dto.getMetrics())
        );
    }

    public DiaryEntryCreateDto toCreateDto(GoalDiaryEntryCommand command) {
        DiaryEntryCreateDto dto = new DiaryEntryCreateDto();
        dto.setWhenStarted(command.whenStarted());
        dto.setWhenEnded(command.whenEnded());
        dto.setMood(command.mood());
        dto.setDescription(command.description());
        dto.setMetrics(toMetricCreateDtos(command.metrics()));
        return dto;
    }

    public DiaryEntryUpdateDto toUpdateDto(GoalDiaryEntryCommand command) {
        DiaryEntryUpdateDto dto = new DiaryEntryUpdateDto();
        dto.setWhenStarted(command.whenStarted());
        dto.setWhenEnded(command.whenEnded());
        dto.setMood(command.mood());
        dto.setDescription(command.description());
        dto.setStatus(command.status());
        dto.setMetrics(toMetricUpdateDtos(command.metrics()));
        return dto;
    }

    public DiaryEntryUpdateDto toFinishedUpdateDto(GoalDiaryEntryCommand command) {
        DiaryEntryUpdateDto dto = toUpdateDto(command);
        dto.setStatus(EntryStatus.FINISHED);
        return dto;
    }

    private List<GoalDiaryEntryCommand.Metric> toMetricCommands(Collection<EntryMetricGoal> metricGoals) {
        if (metricGoals == null) {
            return null;
        }

        List<GoalDiaryEntryCommand.Metric> metricCommands = new ArrayList<>(metricGoals.size());
        for (EntryMetricGoal metricGoal : metricGoals) {
            metricCommands.add(new GoalDiaryEntryCommand.Metric(
                    null,
                    metricGoal.getMetricType().getId(),
                    toMetricValueCommands(metricGoal.getValues())
            ));
        }

        return metricCommands;
    }

    private List<GoalDiaryEntryCommand.Metric> toMetricCommandsFromCreate(List<EntryMetricCreateDto> createDtos) {
        if (createDtos == null) {
            return null;
        }

        List<GoalDiaryEntryCommand.Metric> metricCommands = new ArrayList<>(createDtos.size());
        for (EntryMetricCreateDto createDto : createDtos) {
            metricCommands.add(new GoalDiaryEntryCommand.Metric(
                    null,
                    createDto.getMetricTypeId(),
                    toMetricValueCommandsFromCreate(createDto.getValues())
            ));
        }

        return metricCommands;
    }

    private List<GoalDiaryEntryCommand.Metric> toMetricCommandsFromUpdate(List<EntryMetricUpdateDto> updateDtos) {
        if (updateDtos == null) {
            return null;
        }

        List<GoalDiaryEntryCommand.Metric> metricCommands = new ArrayList<>(updateDtos.size());
        for (EntryMetricUpdateDto updateDto : updateDtos) {
            metricCommands.add(new GoalDiaryEntryCommand.Metric(
                    updateDto.getId(),
                    updateDto.getMetricTypeId(),
                    toMetricValueCommandsFromUpdate(updateDto.getValues())
            ));
        }

        return metricCommands;
    }

    private List<GoalDiaryEntryCommand.Value> toMetricValueCommands(List<EntryMetricValueGoal> valueGoals) {
        if (valueGoals == null) {
            return null;
        }

        List<GoalDiaryEntryCommand.Value> valueCommands = new ArrayList<>(valueGoals.size());
        for (EntryMetricValueGoal valueGoal : valueGoals) {
            valueCommands.add(new GoalDiaryEntryCommand.Value(
                    valueGoal.getUnit().getId(),
                    valueGoal.getExpectedValue()
            ));
        }

        return valueCommands;
    }

    private List<GoalDiaryEntryCommand.Value> toMetricValueCommandsFromCreate(List<EntryMetricValueCreateDto> createDtos) {
        if (createDtos == null) {
            return null;
        }

        List<GoalDiaryEntryCommand.Value> valueCommands = new ArrayList<>(createDtos.size());
        for (EntryMetricValueCreateDto createDto : createDtos) {
            valueCommands.add(new GoalDiaryEntryCommand.Value(
                    createDto.getUnitId(),
                    createDto.getValue()
            ));
        }

        return valueCommands;
    }

    private List<GoalDiaryEntryCommand.Value> toMetricValueCommandsFromUpdate(List<EntryMetricValueUpdateDto> updateDtos) {
        if (updateDtos == null) {
            return null;
        }

        List<GoalDiaryEntryCommand.Value> valueCommands = new ArrayList<>(updateDtos.size());
        for (EntryMetricValueUpdateDto updateDto : updateDtos) {
            valueCommands.add(new GoalDiaryEntryCommand.Value(
                    updateDto.getUnitId(),
                    updateDto.getValue()
            ));
        }

        return valueCommands;
    }

    private List<EntryMetricCreateDto> toMetricCreateDtos(List<GoalDiaryEntryCommand.Metric> metricCommands) {
        if (metricCommands == null) {
            return null;
        }

        List<EntryMetricCreateDto> createDtos = new ArrayList<>(metricCommands.size());
        for (GoalDiaryEntryCommand.Metric metricCommand : metricCommands) {
            EntryMetricCreateDto createDto = new EntryMetricCreateDto();
            createDto.setMetricTypeId(metricCommand.metricTypeId());
            createDto.setValues(toMetricValueCreateDtos(metricCommand.values()));
            createDtos.add(createDto);
        }

        return createDtos;
    }

    private List<EntryMetricUpdateDto> toMetricUpdateDtos(List<GoalDiaryEntryCommand.Metric> metricCommands) {
        if (metricCommands == null) {
            return null;
        }

        List<EntryMetricUpdateDto> updateDtos = new ArrayList<>(metricCommands.size());
        for (GoalDiaryEntryCommand.Metric metricCommand : metricCommands) {
            EntryMetricUpdateDto updateDto = new EntryMetricUpdateDto();
            updateDto.setId(metricCommand.id());
            updateDto.setMetricTypeId(metricCommand.metricTypeId());
            updateDto.setValues(toMetricValueUpdateDtos(metricCommand.values()));
            updateDtos.add(updateDto);
        }

        return updateDtos;
    }

    private List<EntryMetricValueCreateDto> toMetricValueCreateDtos(List<GoalDiaryEntryCommand.Value> valueCommands) {
        if (valueCommands == null) {
            return null;
        }

        List<EntryMetricValueCreateDto> createDtos = new ArrayList<>(valueCommands.size());
        for (GoalDiaryEntryCommand.Value valueCommand : valueCommands) {
            EntryMetricValueCreateDto createDto = new EntryMetricValueCreateDto();
            createDto.setUnitId(valueCommand.unitId());
            createDto.setValue(valueCommand.value());
            createDtos.add(createDto);
        }

        return createDtos;
    }

    private List<EntryMetricValueUpdateDto> toMetricValueUpdateDtos(List<GoalDiaryEntryCommand.Value> valueCommands) {
        if (valueCommands == null) {
            return null;
        }

        List<EntryMetricValueUpdateDto> updateDtos = new ArrayList<>(valueCommands.size());
        for (GoalDiaryEntryCommand.Value valueCommand : valueCommands) {
            EntryMetricValueUpdateDto updateDto = new EntryMetricValueUpdateDto();
            updateDto.setUnitId(valueCommand.unitId());
            updateDto.setValue(valueCommand.value());
            updateDtos.add(updateDto);
        }

        return updateDtos;
    }
}
