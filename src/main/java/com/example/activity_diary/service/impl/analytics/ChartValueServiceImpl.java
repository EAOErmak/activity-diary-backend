package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.service.analytics.ChartCalculationStrategy;
import com.example.activity_diary.service.analytics.ChartValueService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChartValueServiceImpl implements ChartValueService {

    private final List<ChartCalculationStrategy> strategies;

    private final Map<ChartType, ChartCalculationStrategy> strategyMap =
            new EnumMap<>(ChartType.class);

    @PostConstruct
    public void init() {
        for (ChartCalculationStrategy strategy : strategies) {
            ChartType type = strategy.supportedType();

            if (strategyMap.containsKey(type)) {
                throw new IllegalStateException(
                        "Duplicate ChartCalculationStrategy for type: " + type
                );
            }

            strategyMap.put(type, strategy);
        }
    }

    @Override
    public Double calculateY(DiaryEntry entry) {

        if (entry == null) {
            return 0.0;
        }

        if (entry.getCategory() == null) {
            throw new IllegalStateException(
                    "DiaryEntry.whatHappened must not be null for analytics"
            );
        }

        ChartType type = entry.getCategory().getChartType();

        if (type == null) {
            throw new BadRequestException(
                    "ChartType is not configured for this category"
            );
        }

        ChartCalculationStrategy strategy = strategyMap.get(type);

        if (strategy == null) {
            throw new IllegalStateException(
                    "No ChartCalculationStrategy registered for type: " + type
            );
        }

        BigDecimal value = strategy.calculateY(entry);

        return value == null ? 0.0 : value.doubleValue();
    }
}
