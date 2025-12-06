package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.entity.ActivityItem;
import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CountPerDayChartStrategy implements ChartCalculationStrategy {

    @Override
    public boolean supports(ChartType chartType) {
        return chartType == ChartType.COUNT_PER_DAY;
    }

    @Override
    public BigDecimal calculateY(DiaryEntry entry) {
        if (entry == null || entry.getWhatDidYouDo() == null) {
            return BigDecimal.ZERO;
        }

        return entry.getWhatDidYouDo().stream()
                .map(ActivityItem::getCount)
                .map(count -> count == null ? BigDecimal.ZERO : BigDecimal.valueOf(count))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
