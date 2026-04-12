package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.dto.analytics.ChartPointDto;
import com.example.activity_diary.dto.analytics.ChartResponseDto;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SleepScorePerEntryStrategy implements ChartCalculationStrategy {

    private static final String SCORE_LABEL = "score";
    private static final BigDecimal OPTIMAL_DURATION_MINUTES = BigDecimal.valueOf(8L * 60L);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final DiaryEntryChartSupportService diaryEntryChartSupportService;

    public SleepScorePerEntryStrategy(DiaryEntryChartSupportService diaryEntryChartSupportService) {
        this.diaryEntryChartSupportService = diaryEntryChartSupportService;
    }

    @Override
    public ChartResponseDto calculate(Long userId, ChartFilterDto filter) {
        return diaryEntryChartSupportService.buildSinglePointPerEntryChart(
                getChartType(),
                userId,
                filter,
                entry -> new ChartPointDto(SCORE_LABEL, calculateSleepScore(entry))
        );
    }

    @Override
    public ChartType getChartType() {
        return ChartType.SLEEP_SCORE_PER_ENTRY;
    }

    private BigDecimal calculateSleepScore(DiaryEntry entry) {
        int duration = entry.getDuration() == null ? 0 : Math.max(entry.getDuration(), 0);

        return BigDecimal.valueOf(duration)
                .divide(OPTIMAL_DURATION_MINUTES, 6, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE)
                .multiply(ONE_HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
