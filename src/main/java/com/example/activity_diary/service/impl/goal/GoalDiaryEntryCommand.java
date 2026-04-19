package com.example.activity_diary.service.impl.goal;

import com.example.activity_diary.entity.enums.EntryStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

record GoalDiaryEntryCommand(
        Instant whenStarted,
        Instant whenEnded,
        Short mood,
        String description,
        EntryStatus status,
        List<Metric> metrics
) {

    record Metric(
            Long id,
            Long metricTypeId,
            List<Value> values
    ) {
    }

    record Value(
            Long unitId,
            BigDecimal value
    ) {
    }
}
