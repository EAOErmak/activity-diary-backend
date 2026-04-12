package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartPointDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DurationPerEntryStrategy implements ChartCalculationStrategy {

    private static final String DURATION_LABEL = "duration";

    private final DiaryEntryChartSupportService diaryEntryChartSupportService;

    public DurationPerEntryStrategy(DiaryEntryChartSupportService diaryEntryChartSupportService) {
        this.diaryEntryChartSupportService = diaryEntryChartSupportService;
    }

    @Override
    public ChartResponseDto calculate(Long userId, ChartFilterDto filter) {
        return diaryEntryChartSupportService.buildSinglePointPerEntryChart(
                getChartType(),
                userId,
                filter,
                entry -> new ChartPointDto(DURATION_LABEL, extractDuration(entry))
        );
    }

    @Override
    public ChartType getChartType() {
        return ChartType.DURATION_PER_ENTRY;
    }

    private BigDecimal extractDuration(DiaryEntry entry) {
        int duration = entry.getDuration() == null ? 0 : Math.max(entry.getDuration(), 0);
        return BigDecimal.valueOf(duration);
    }
}
