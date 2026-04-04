package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.repository.diary.DiaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingRawChartStrategyTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Test
    void calculate_appliesDateRangeFilter() {
        TrainingRawChartStrategy strategy = new TrainingRawChartStrategy(diaryRepository);
        Instant dateFrom = Instant.parse("2026-02-01T00:00:00Z");
        Instant dateTo = Instant.parse("2026-02-10T00:00:00Z");
        ChartFilterDto filter = new ChartFilterDto(7L, dateFrom, dateTo, ChartType.TRAINING_RAW);

        when(diaryRepository.findAllByUserIdAndTagIdAndWhenStartedRange(11L, 7L, dateFrom, dateTo))
                .thenReturn(List.of(diaryEntryWithMetricValue("km", 5)));

        ChartResponseDto response = strategy.calculate(11L, filter);

        verify(diaryRepository).findAllByUserIdAndTagIdAndWhenStartedRange(11L, 7L, dateFrom, dateTo);
        verify(diaryRepository, never()).findAllByUserIdAndTags_Id(11L, 7L);
        assertEquals(ChartType.TRAINING_RAW, response.getChartType());
        assertEquals(1, response.getSeries().size());
        assertEquals(1, response.getSeries().getFirst().getPoints().size());
        assertEquals("km", response.getSeries().getFirst().getPoints().getFirst().getLabel());
        assertEquals(new BigDecimal("5"), response.getSeries().getFirst().getPoints().getFirst().getValue());
    }

    private static DiaryEntry diaryEntryWithMetricValue(String unitLabel, int value) {
        DiaryEntry entry = DiaryEntry.builder().build();
        DictionaryItem metricType = DictionaryItem.builder().label("distance").build();
        DictionaryItem unit = DictionaryItem.builder().label(unitLabel).build();

        EntryMetric metric = EntryMetric.create(entry, metricType);
        metric.addValue(unit, value);
        entry.addMetric(metric);

        return entry;
    }
}
