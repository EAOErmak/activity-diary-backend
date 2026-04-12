package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.entity.diary.DiaryEntry;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SleepScorePerEntryStrategyTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Test
    void calculate_returnsCappedSleepScorePerEntryInChronologicalOrder() {
        DiaryEntryChartSupportService supportService = new DiaryEntryChartSupportService(diaryRepository);
        SleepScorePerEntryStrategy strategy = new SleepScorePerEntryStrategy(supportService);

        Instant dateFrom = Instant.parse("2026-02-01T00:00:00Z");
        Instant dateTo = Instant.parse("2026-02-10T00:00:00Z");
        ChartFilterDto filter = new ChartFilterDto(7L, dateFrom, dateTo, ChartType.SLEEP_SCORE_PER_ENTRY);

        DiaryEntry second = diaryEntry(2L, Instant.parse("2026-02-03T23:00:00Z"), 600);
        DiaryEntry first = diaryEntry(1L, Instant.parse("2026-02-01T23:00:00Z"), 120);
        DiaryEntry third = diaryEntry(3L, Instant.parse("2026-02-05T23:00:00Z"), 480);

        when(diaryRepository.findAllByUserIdAndTagIdAndWhenStartedRange(11L, 7L, dateFrom, dateTo))
                .thenReturn(List.of(second, third, first));

        ChartResponseDto response = strategy.calculate(11L, filter);

        assertEquals(ChartType.SLEEP_SCORE_PER_ENTRY, response.getChartType());
        assertEquals(3, response.getSeries().size());

        assertEquals("score", response.getSeries().get(0).getPoints().get(0).getLabel());
        assertEquals(new BigDecimal("25.00"), response.getSeries().get(0).getPoints().get(0).getValue());
        assertEquals(new BigDecimal("100.00"), response.getSeries().get(1).getPoints().get(0).getValue());
        assertEquals(new BigDecimal("100.00"), response.getSeries().get(2).getPoints().get(0).getValue());
    }

    private static DiaryEntry diaryEntry(Long id, Instant whenStarted, Integer duration) {
        DiaryEntry entry = DiaryEntry.builder()
                .whenStarted(whenStarted)
                .duration(duration)
                .build();
        entry.setId(id);
        return entry;
    }
}
