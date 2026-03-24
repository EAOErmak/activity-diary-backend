package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricCreateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricUpdateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueCreateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueUpdateDto;
import com.example.activity_diary.exception.types.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiaryValidationServiceImplTest {

    private final DiaryValidationServiceImpl service = new DiaryValidationServiceImpl();

    @Test
    void validateCreate_withValidData_doesNotThrow() {
        DiaryEntryCreateDto dto = validCreateDto();

        assertDoesNotThrow(() -> service.validateCreate(dto));
    }

    @Test
    void validateCreate_missingTime_throwsBadRequest() {
        DiaryEntryCreateDto dto = validCreateDto();
        dto.setWhenStarted(null);

        assertThrows(BadRequestException.class, () -> service.validateCreate(dto));
    }

    @Test
    void validateCreate_endBeforeStart_throwsBadRequest() {
        DiaryEntryCreateDto dto = validCreateDto();
        Instant start = Instant.parse("2026-02-10T10:00:00Z");
        dto.setWhenStarted(start);
        dto.setWhenEnded(start.minusSeconds(60));

        assertThrows(BadRequestException.class, () -> service.validateCreate(dto));
    }

    @Test
    void validateCreate_durationTooShort_throwsBadRequest() {
        DiaryEntryCreateDto dto = validCreateDto();
        Instant start = Instant.parse("2026-02-10T10:00:00Z");
        dto.setWhenStarted(start);
        dto.setWhenEnded(start.plusSeconds(30));

        assertThrows(BadRequestException.class, () -> service.validateCreate(dto));
    }

    @Test
    void validateCreate_invalidMood_throwsBadRequest() {
        DiaryEntryCreateDto dto = validCreateDto();
        dto.setMood((short) 0);

        assertThrows(BadRequestException.class, () -> service.validateCreate(dto));
    }

    @Test
    void validateCreate_duplicateMetricTypeId_doesNotThrow() {
        DiaryEntryCreateDto dto = validCreateDto();
        EntryMetricCreateDto metric1 = metricCreate(1L, 10L);
        EntryMetricCreateDto metric2 = metricCreate(1L, 11L);
        dto.setMetrics(List.of(metric1, metric2));

        assertDoesNotThrow(() -> service.validateCreate(dto));
    }

    @Test
    void validateUpdate_duplicateMetricTypeId_doesNotThrow() {
        DiaryEntryUpdateDto dto = new DiaryEntryUpdateDto();
        dto.setWhenStarted(Instant.parse("2026-02-10T10:00:00Z"));
        dto.setWhenEnded(Instant.parse("2026-02-10T10:10:00Z"));
        dto.setMood((short) 3);
        dto.setDescription("ok");
        dto.setMetrics(List.of(metricUpdate(1L, 10L), metricUpdate(1L, 11L)));

        assertDoesNotThrow(() -> service.validateUpdate(dto));
    }

    @Test
    void validateUpdate_onlyStartProvided_throwsBadRequest() {
        DiaryEntryUpdateDto dto = new DiaryEntryUpdateDto();
        dto.setWhenStarted(Instant.parse("2026-02-10T10:00:00Z"));
        dto.setMood((short) 3);
        dto.setDescription("ok");
        dto.setMetrics(List.of(metricUpdate(1L, 10L)));

        assertThrows(BadRequestException.class, () -> service.validateUpdate(dto));
    }

    private static DiaryEntryCreateDto validCreateDto() {
        DiaryEntryCreateDto dto = new DiaryEntryCreateDto();
        dto.setWhenStarted(Instant.parse("2026-02-10T10:00:00Z"));
        dto.setWhenEnded(Instant.parse("2026-02-10T10:10:00Z"));
        dto.setMood((short) 3);
        dto.setDescription("ok");
        dto.setMetrics(List.of(metricCreate(1L, 10L)));
        return dto;
    }

    private static EntryMetricCreateDto metricCreate(Long metricTypeId, Long unitId) {
        EntryMetricValueCreateDto value = new EntryMetricValueCreateDto();
        value.setUnitId(unitId);
        value.setValue(1);

        EntryMetricCreateDto metric = new EntryMetricCreateDto();
        metric.setMetricTypeId(metricTypeId);
        metric.setValues(List.of(value));
        return metric;
    }

    private static EntryMetricUpdateDto metricUpdate(Long metricTypeId, Long unitId) {
        EntryMetricValueUpdateDto value = new EntryMetricValueUpdateDto();
        value.setUnitId(unitId);
        value.setValue(1);

        EntryMetricUpdateDto metric = new EntryMetricUpdateDto();
        metric.setMetricTypeId(metricTypeId);
        metric.setValues(List.of(value));
        return metric;
    }
}
