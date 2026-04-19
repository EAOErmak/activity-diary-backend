package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricCreateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricUpdateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueCreateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueUpdateDto;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.enums.EntryStatusPolicy;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.service.diary.DiaryValidationService;
import com.example.activity_diary.util.MetricValueNormalizer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DiaryValidationServiceImpl implements DiaryValidationService {

    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final short MIN_MOOD = 1;
    private static final short MAX_MOOD = 5;

    @Override
    public void validateCreate(DiaryEntryCreateDto dto) {

        validateTime(dto.getWhenStarted(), dto.getWhenEnded());

        validateMood(dto.getMood());

        validateDescription(dto.getDescription());

        validateMetricsCreate(dto.getMetrics());
    }

    @Override
    public void validateUpdate(DiaryEntryUpdateDto dto) {

        if (dto.getWhenStarted() != null || dto.getWhenEnded() != null) {
            validateTime(dto.getWhenStarted(), dto.getWhenEnded());
        }

        validateMood(dto.getMood());

        validateDescription(dto.getDescription());

        validateStatus(dto.getStatus());

        validateMetricsUpdate(dto.getMetrics());
    }

    private void validateTime(Instant start, Instant end) {

        if (start == null || end == null) {
            throw new BadRequestException("whenStarted and whenEnded are required");
        }

        if (!end.isAfter(start)) {
            throw new BadRequestException("whenEnded must be after whenStarted");
        }

        long minutes = Duration.between(start, end).toMinutes();
        if (minutes < 1) {
            throw new BadRequestException("Duration must be at least 1 minute");
        }
    }

    private void validateMood(Short mood) {

        if (mood == null) return;

        if (mood < MIN_MOOD || mood > MAX_MOOD) {
            throw new BadRequestException("Mood must be between 1 and 5");
        }
    }

    private void validateDescription(String desc) {

        if (desc == null) return;
        if (desc.isBlank()) {
            throw new BadRequestException("Description is required");
        }
        if (desc.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BadRequestException("Description is too long");
        }
    }

    private void validateMetricsCreate(List<EntryMetricCreateDto> metrics) {
        if (metrics == null || metrics.isEmpty()) return;

        for (EntryMetricCreateDto metric : metrics) {
            if (metric == null) {
                throw new BadRequestException("Metric cannot be null");
            }

            Long metricTypeId = metric.getMetricTypeId();
            if (metricTypeId == null) {
                throw new BadRequestException("metricTypeId is required");
            }

            validateMetricValues(metricTypeId, metric.getValues());
        }
    }

    private void validateMetricsUpdate(List<EntryMetricUpdateDto> metrics) {
        if (metrics == null || metrics.isEmpty()) return;

        for (EntryMetricUpdateDto metric : metrics) {
            if (metric == null) {
                throw new BadRequestException("Metric cannot be null");
            }

            Long metricTypeId = metric.getMetricTypeId();
            if (metricTypeId == null) {
                throw new BadRequestException("metricTypeId is required");
            }

            validateMetricValues(metricTypeId, metric.getValues());
        }
    }

    private void validateMetricValues(Long metricTypeId, List<?> values) {
        if (values == null || values.isEmpty()) {
            throw new BadRequestException("Values are required for metricTypeId: " + metricTypeId);
        }

        Set<Long> unitIds = new HashSet<>();

        for (Object raw : values) {
            if (raw == null) {
                throw new BadRequestException("Metric value cannot be null for metricTypeId: " + metricTypeId);
            }

            Long unitId;
            BigDecimal value;
            if (raw instanceof EntryMetricValueCreateDto v) {
                unitId = v.getUnitId();
                value = v.getValue();
            } else if (raw instanceof EntryMetricValueUpdateDto v) {
                unitId = v.getUnitId();
                value = v.getValue();
            } else {
                throw new BadRequestException("Invalid metric value type for metricTypeId: " + metricTypeId);
            }

            if (unitId == null) {
                throw new BadRequestException("unitId is required for metricTypeId: " + metricTypeId);
            }

            if (!unitIds.add(unitId)) {
                throw new BadRequestException("Duplicate unitId " + unitId + " for metricTypeId: " + metricTypeId);
            }

            validateMetricValue(metricTypeId, unitId, value);
        }
    }

    private void validateMetricValue(Long metricTypeId, Long unitId, BigDecimal value) {
        try {
            MetricValueNormalizer.normalizePositive(value, "Value");
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    "value must be greater than zero for unitId " + unitId + " and metricTypeId: " + metricTypeId
            );
        }
    }

    private void validateStatus(EntryStatus status) {
        if (status != null && !EntryStatusPolicy.canBeSetManually(status)) {
            throw new BadRequestException("OVERDUE is assigned automatically");
        }
    }
}
