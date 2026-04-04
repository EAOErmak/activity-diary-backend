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
    void toCreateDto_mapsGoalFieldsAndMetrics() {
        DiaryEntryGoal goal = DiaryEntryGoal.builder()
                .user(User.builder().username("user").build())
                .whenStarted(Instant.parse("2026-04-01T08:00:00Z"))
                .whenEnded(Instant.parse("2026-04-01T09:00:00Z"))
                .mood((short) 4)
                .description("goal #tag")
                .build();
        goal.addMetricGoal(metricGoal(goal, 100L, 200L, 12));

        DiaryEntryCreateDto result = factory.toCreateDto(goal);

        assertEquals(goal.getWhenStarted(), result.getWhenStarted());
        assertEquals(goal.getWhenEnded(), result.getWhenEnded());
        assertEquals(goal.getMood(), result.getMood());
        assertEquals(goal.getDescription(), result.getDescription());
        assertNotNull(result.getMetrics());
        assertEquals(1, result.getMetrics().size());
        assertEquals(100L, result.getMetrics().getFirst().getMetricTypeId());
        assertEquals(200L, result.getMetrics().getFirst().getValues().getFirst().getUnitId());
        assertEquals(12, result.getMetrics().getFirst().getValues().getFirst().getValue());
    }

    @Test
    void toUpdateDto_mapsCreateCommandAndForcesFinishedStatus() {
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

        DiaryEntryUpdateDto result = factory.toUpdateDto(createDto);

        assertEquals(createDto.getWhenStarted(), result.getWhenStarted());
        assertEquals(createDto.getWhenEnded(), result.getWhenEnded());
        assertEquals(createDto.getMood(), result.getMood());
        assertEquals(createDto.getDescription(), result.getDescription());
        assertEquals(EntryStatus.FINISHED, result.getStatus());
        assertNotNull(result.getMetrics());
        assertEquals(1, result.getMetrics().size());
        assertEquals(100L, result.getMetrics().getFirst().getMetricTypeId());
        assertEquals(200L, result.getMetrics().getFirst().getValues().getFirst().getUnitId());
        assertEquals(18, result.getMetrics().getFirst().getValues().getFirst().getValue());
    }

    @Test
    void toUpdateDto_keepsNullMetricsAsNull() {
        DiaryEntryCreateDto createDto = new DiaryEntryCreateDto();
        createDto.setDescription("updated #tag");

        DiaryEntryUpdateDto result = factory.toUpdateDto(createDto);

        assertNull(result.getMetrics());
        assertEquals(EntryStatus.FINISHED, result.getStatus());
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
