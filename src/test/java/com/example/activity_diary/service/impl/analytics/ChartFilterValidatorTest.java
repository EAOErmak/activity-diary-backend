package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.exception.types.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChartFilterValidatorTest {

    private final ChartFilterValidator validator = new ChartFilterValidator();

    @Test
    void validate_nullFilter_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> validator.validate(null));
    }

    @Test
    void validate_missingTagId_throwsBadRequest() {
        ChartFilterDto filter = new ChartFilterDto(
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z"),
                ChartType.TRAINING_RAW
        );

        assertThrows(BadRequestException.class, () -> validator.validate(filter));
    }

    @Test
    void validate_missingChartType_throwsBadRequest() {
        ChartFilterDto filter = new ChartFilterDto(
                7L,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z"),
                null
        );

        assertThrows(BadRequestException.class, () -> validator.validate(filter));
    }

    @Test
    void validate_validFilter_doesNotThrow() {
        ChartFilterDto filter = new ChartFilterDto(
                7L,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z"),
                ChartType.TRAINING_RAW
        );

        assertDoesNotThrow(() -> validator.validate(filter));
    }
}
