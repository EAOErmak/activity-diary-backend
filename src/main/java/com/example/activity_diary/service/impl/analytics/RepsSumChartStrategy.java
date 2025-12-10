package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.entity.EntryMetric;
import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RepsSumChartStrategy implements ChartCalculationStrategy {

    @Override
    public ChartType supportedType() {
        return ChartType.REPS_SUM;
    }

    @Override
    public BigDecimal calculateY(DiaryEntry entry) {

        return entry.getMetrics().stream()
                .map(EntryMetric::getValue)
                .map(count -> count == null
                        ? BigDecimal.ZERO
                        : BigDecimal.valueOf(count)
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
