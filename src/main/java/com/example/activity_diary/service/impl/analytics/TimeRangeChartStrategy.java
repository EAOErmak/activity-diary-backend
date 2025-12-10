package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TimeRangeChartStrategy implements ChartCalculationStrategy {

    private static final BigDecimal MINUTES_IN_HOUR = BigDecimal.valueOf(60);

    @Override
    public ChartType supportedType() {
        return ChartType.TIME_RANGE;
    }

    @Override
    public BigDecimal calculateY(DiaryEntry entry) {

        if (entry.getDuration() == null) {
            return BigDecimal.ZERO;
        }

        // duration хранится в минутах → перевод в часы
        return BigDecimal.valueOf(entry.getDuration())
                .divide(MINUTES_IN_HOUR, 2, RoundingMode.HALF_UP);
    }
}
