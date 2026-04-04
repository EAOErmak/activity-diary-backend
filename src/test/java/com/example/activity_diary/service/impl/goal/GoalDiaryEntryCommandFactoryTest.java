package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.goal.DiaryEntryGoal;
import com.example.activity_diary.entity.goal.EntryMetricGoal;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GoalDiaryEntryCommandFactoryTest {

    private final GoalDiaryEntryCommandFactory factory = new GoalDiaryEntryCommandFactory();

    @Test
    void fromGoal_mapsGoalFieldsAndMetrics() {
        DiaryEntryGoal goal = DiaryEntryGoal.builder()
                .user(User.builder().username("user").build())
                .whenStarted(Instant.parse("2026-04-01T08:00:00Z"))
                .whenEnded(Instant.parse("2026-04-01T09:00:00Z"))
                .mood((short) 4)
                .description("goal #tag")
                .build();
        goal.addMetricGoal(metricGoal(goal, 100L, 200L, 12));

        GoalDiaryEntryCommand result = factory.fromGoal(goal);

        assertEquals(goal.getWhenStarted(), result.whenStarted());
        assertEquals(goal.getWhenEnded(), result.whenEnded());
        assertEquals(goal.getMood(), result.mood());
        assertEquals(goal.getDescription(), result.description());
        assertNotNull(result.metrics());
        assertEquals(1, result.metrics().size());
        assertNull(result.metrics().getFirst().id());
        assertEquals(100L, result.metrics().getFirst().metricTypeId());
        assertEquals(200L, result.metrics().getFirst().values().getFirst().unitId());
        assertEquals(12, result.metrics().getFirst().values().getFirst().value());
    }

    @Test
    void fromCreateDto_mapsDiaryCreateDtoToInternalCommand() {
        DiaryEntryCreateDto createDto = new DiaryEntryCreateDto();
        createDto.setWhenStarted(Instant.parse("2026-04-01T08:00:00Z"));
        createDto.setWhenEnded(Instant.parse("2026-04-01T09:00:00Z"));
        createDto.setMood((short) 5);
        createDto.setDescription("updated #tag");

        var valueDto = new com.example.activity_diary.dto.diary.metric.EntryMetricValueCreateDto();
        valueDto.setUnitId(200L);
        valueDto.setValue(18);

        var metricDto = new com.example.activity_diary.dto.diary.metric.EntryMetricCreateDto();
        metricDto.setMetricTypeId(100L);
        metricDto.setValues(List.of(valueDto));

        createDto.setMetrics(List.of(metricDto));

        GoalDiaryEntryCommand result = factory.fromCreateDto(createDto);

        assertEquals(createDto.getWhenStarted(), result.whenStarted());
        assertEquals(createDto.getWhenEnded(), result.whenEnded());
        assertEquals(createDto.getMood(), result.mood());
        assertEquals(createDto.getDescription(), result.description());
        assertNull(result.status());
        assertNotNull(result.metrics());
        assertEquals(1, result.metrics().size());
        assertNull(result.metrics().getFirst().id());
        assertEquals(100L, result.metrics().getFirst().metricTypeId());
        assertEquals(200L, result.metrics().getFirst().values().getFirst().unitId());
        assertEquals(18, result.metrics().getFirst().values().getFirst().value());
    }

    @Test
    void fromUpdateDto_preservesStatusAndMetricId() {
        DiaryEntryUpdateDto updateDto = new DiaryEntryUpdateDto();
        updateDto.setWhenStarted(Instant.parse("2026-04-01T08:00:00Z"));
        updateDto.setWhenEnded(Instant.parse("2026-04-01T09:00:00Z"));
        updateDto.setMood((short) 3);
        updateDto.setDescription("updated #tag");
        updateDto.setStatus(EntryStatus.ACTIVE);

        var valueDto = new com.example.activity_diary.dto.diary.metric.EntryMetricValueUpdateDto();
        valueDto.setUnitId(200L);
        valueDto.setValue(9);

        var metricDto = new com.example.activity_diary.dto.diary.metric.EntryMetricUpdateDto();
        metricDto.setId(777L);
        metricDto.setMetricTypeId(100L);
        metricDto.setValues(List.of(valueDto));

        updateDto.setMetrics(List.of(metricDto));

        GoalDiaryEntryCommand result = factory.fromUpdateDto(updateDto);

        assertEquals(EntryStatus.ACTIVE, result.status());
        assertEquals(777L, result.metrics().getFirst().id());
        assertEquals(100L, result.metrics().getFirst().metricTypeId());
        assertEquals(200L, result.metrics().getFirst().values().getFirst().unitId());
        assertEquals(9, result.metrics().getFirst().values().getFirst().value());
    }

    @Test
    void toFinishedUpdateDto_forcesFinishedStatus() {
        GoalDiaryEntryCommand command = new GoalDiaryEntryCommand(
                Instant.parse("2026-04-01T08:00:00Z"),
                Instant.parse("2026-04-01T09:00:00Z"),
                (short) 5,
                "updated #tag",
                null,
                List.of(new GoalDiaryEntryCommand.Metric(
                        777L,
                        100L,
                        List.of(new GoalDiaryEntryCommand.Value(200L, 18))
                ))
        );

        DiaryEntryUpdateDto result = factory.toFinishedUpdateDto(command);

        assertEquals(command.whenStarted(), result.getWhenStarted());
        assertEquals(command.whenEnded(), result.getWhenEnded());
        assertEquals(command.mood(), result.getMood());
        assertEquals(command.description(), result.getDescription());
        assertEquals(EntryStatus.FINISHED, result.getStatus());
        assertNotNull(result.getMetrics());
        assertEquals(1, result.getMetrics().size());
        assertEquals(777L, result.getMetrics().getFirst().getId());
    }

    @Test
    void toCreateDto_mapsInternalCommandToDiaryCreateDto() {
        GoalDiaryEntryCommand command = new GoalDiaryEntryCommand(
                Instant.parse("2026-04-01T08:00:00Z"),
                Instant.parse("2026-04-01T09:00:00Z"),
                (short) 4,
                "goal #tag",
                null,
                List.of(new GoalDiaryEntryCommand.Metric(
                        null,
                        100L,
                        List.of(new GoalDiaryEntryCommand.Value(200L, 12))
                ))
        );

        DiaryEntryCreateDto result = factory.toCreateDto(command);

        assertEquals(command.whenStarted(), result.getWhenStarted());
        assertEquals(command.whenEnded(), result.getWhenEnded());
        assertEquals(command.mood(), result.getMood());
        assertEquals(command.description(), result.getDescription());
        assertNotNull(result.getMetrics());
        assertEquals(100L, result.getMetrics().getFirst().getMetricTypeId());
        assertEquals(200L, result.getMetrics().getFirst().getValues().getFirst().getUnitId());
    }

    @Test
    void toUpdateDto_keepsCommandStatusAndNullMetrics() {
        GoalDiaryEntryCommand command = new GoalDiaryEntryCommand(
                null,
                null,
                null,
                "updated #tag",
                EntryStatus.ACTIVE,
                null
        );

        DiaryEntryUpdateDto result = factory.toUpdateDto(command);

        assertNull(result.getMetrics());
        assertEquals(EntryStatus.ACTIVE, result.getStatus());
    }

    private static EntryMetricGoal metricGoal(DiaryEntryGoal goal, Long metricTypeId, Long unitId, Integer value) {
        DictionaryItem metricType = DictionaryItem.builder()
                .type(DictionaryType.METRIC_NAME)
                .label("metric")
                .build();
        metricType.setId(metricTypeId);

        DictionaryItem unit = DictionaryItem.builder()
                .type(DictionaryType.METRIC_UNIT)
                .label("unit")
                .build();
        unit.setId(unitId);

        EntryMetricGoal metricGoal = EntryMetricGoal.create(goal, metricType, 1);
        metricGoal.addValue(unit, value);
        return metricGoal;
    }
}
