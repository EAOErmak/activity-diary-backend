package com.example.activity_diary.service.analytics;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.enums.ChartType;

import java.math.BigDecimal;

public interface ChartCalculationStrategy {

    boolean supports(ChartType chartType);

    BigDecimal calculateY(DiaryEntry entry);
}
