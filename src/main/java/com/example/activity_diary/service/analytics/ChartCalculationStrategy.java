package com.example.activity_diary.service.analytics;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.enums.ChartType;

import java.math.BigDecimal;

public interface ChartCalculationStrategy {

    ChartType supportedType();

    BigDecimal calculateY(DiaryEntry entry);
}

