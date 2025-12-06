package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TimeRangeChartStrategy implements ChartCalculationStrategy {

    @Override
    public boolean supports(ChartType chartType) {
        return chartType == ChartType.TIME_RANGE;
    }

    @Override
    public BigDecimal calculateY(DiaryEntry entry) {
        if (entry == null || entry.getDuration() == null) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(entry.getDuration())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP); // часы
    }
}
