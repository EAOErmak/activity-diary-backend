package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import com.example.activity_diary.service.analytics.ChartValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChartValueServiceImpl implements ChartValueService {

    private final List<ChartCalculationStrategy> strategies;

    @Override
    public Double calculateY(DiaryEntry entry) {
        if (entry == null || entry.getWhatHappened() == null) {
            return 0.0;
        }

        ChartType type = entry.getWhatHappened().getChartType();
        if (type == null) {
            return 0.0;
        }

        ChartCalculationStrategy strategy = strategies.stream()
                .filter(s -> s.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No ChartCalculationStrategy found for type: " + type));

        BigDecimal value = strategy.calculateY(entry);
        return value == null ? 0.0 : value.doubleValue();
    }
}
