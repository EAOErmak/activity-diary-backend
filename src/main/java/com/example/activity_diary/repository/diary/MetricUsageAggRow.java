package com.example.activity_diary.repository.diary;

import com.example.activity_diary.entity.enums.TagUsageBucket;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MetricUsageAggRow {
    Long getMetricTypeId();

    String getMetricTypeLabel();

    Long getUnitId();

    String getUnitLabel();

    TagUsageBucket getBucket();

    LocalDate getBucketStart();

    BigDecimal getValueSum();

    int getValueCount();
}
