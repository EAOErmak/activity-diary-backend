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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DurationPerEntryStrategyTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Test
    void calculate_returnsRawDurationPerEntry() {
        DiaryEntryChartSupportService supportService = new DiaryEntryChartSupportService(diaryRepository);
        DurationPerEntryStrategy strategy = new DurationPerEntryStrategy(supportService);

        Instant dateFrom = Instant.parse("2026-03-01T00:00:00Z");
        Instant dateTo = Instant.parse("2026-03-10T00:00:00Z");
        ChartFilterDto filter = new ChartFilterDto(9L, dateFrom, dateTo, ChartType.DURATION_PER_ENTRY);

        DiaryEntry later = diaryEntry(2L, Instant.parse("2026-03-04T08:00:00Z"), 90);
        DiaryEntry earlier = diaryEntry(1L, Instant.parse("2026-03-02T08:00:00Z"), 45);

        when(diaryRepository.findAllByUserIdAndTagIdAndWhenStartedRange(12L, 9L, dateFrom, dateTo))
                .thenReturn(List.of(later, earlier));

        ChartResponseDto response = strategy.calculate(12L, filter);

        verify(diaryRepository).findAllByUserIdAndTagIdAndWhenStartedRange(12L, 9L, dateFrom, dateTo);
        assertEquals(ChartType.DURATION_PER_ENTRY, response.getChartType());
        assertEquals(2, response.getSeries().size());
        assertEquals("duration", response.getSeries().get(0).getPoints().get(0).getLabel());
        assertEquals(new BigDecimal("45"), response.getSeries().get(0).getPoints().get(0).getValue());
        assertEquals(new BigDecimal("90"), response.getSeries().get(1).getPoints().get(0).getValue());
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
