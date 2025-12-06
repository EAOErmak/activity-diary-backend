package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MoodAverageChartStrategy implements ChartCalculationStrategy {

    @Override
    public boolean supports(ChartType chartType) {
        return chartType == ChartType.MOOD_AVERAGE;
    }

    @Override
    public BigDecimal calculateY(DiaryEntry entry) {
        if (entry == null || entry.getHowYouWereFeeling() == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(entry.getHowYouWereFeeling());
    }
}
