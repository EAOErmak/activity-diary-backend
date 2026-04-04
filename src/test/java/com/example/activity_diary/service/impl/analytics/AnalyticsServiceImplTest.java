package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import com.example.activity_diary.service.analytics.TagChartTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private ChartCalculationStrategy strategy;

    @Mock
    private TagChartTypeService tagChartTypeService;

    @Test
    void getChart_validatesAllowedChartTypeBeforeCalculation() {
        when(strategy.getChartType()).thenReturn(ChartType.TRAINING_RAW);

        AnalyticsServiceImpl service = new AnalyticsServiceImpl(List.of(strategy), tagChartTypeService);
        ChartFilterDto filter = new ChartFilterDto(
                7L,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z"),
                ChartType.TRAINING_RAW
        );
        ChartResponseDto response = new ChartResponseDto();

        when(strategy.calculate(11L, filter)).thenReturn(response);

        ChartResponseDto result = service.getChart(11L, filter);

        assertSame(response, result);
        verify(tagChartTypeService).validateChartTypeAllowed(7L, ChartType.TRAINING_RAW);
        verify(strategy).calculate(11L, filter);
    }

    @Test
    void getChart_withoutSupportedStrategy_throwsBadRequest() {
        AnalyticsServiceImpl service = new AnalyticsServiceImpl(List.of(), tagChartTypeService);
        ChartFilterDto filter = new ChartFilterDto(
                7L,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z"),
                ChartType.CALORIES_PER_DAY
        );

        assertThrows(BadRequestException.class, () -> service.getChart(11L, filter));
        verify(tagChartTypeService).validateChartTypeAllowed(7L, ChartType.CALORIES_PER_DAY);
    }

    @Test
    void getChart_withoutTagId_throwsBadRequest() {
        AnalyticsServiceImpl service = new AnalyticsServiceImpl(List.of(), tagChartTypeService);
        ChartFilterDto filter = new ChartFilterDto(
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z"),
                ChartType.TRAINING_RAW
        );

        assertThrows(BadRequestException.class, () -> service.getChart(11L, filter));
    }
}
