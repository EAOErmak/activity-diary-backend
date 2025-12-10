package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MoodAverageChartStrategy implements ChartCalculationStrategy {

    @Override
    public ChartType supportedType() {
        return ChartType.MOOD_AVERAGE;
    }

    @Override
    public BigDecimal calculateY(DiaryEntry entry) {

        if (entry.getMood() == null) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(entry.getMood());
    }
}
