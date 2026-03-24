package com.example.activity_diary.service.analytics;

import com.example.activity_diary.dto.analytics.MetricUsageAggDto;
import com.example.activity_diary.dto.analytics.MetricUsageAggFilterDto;
import com.example.activity_diary.entity.diary.DiaryEntry;

import java.util.List;

public interface MetricUsageAggService {
    List<MetricUsageAggDto> getUsage(Long userId, MetricUsageAggFilterDto filter);

    void onEntryCreated(DiaryEntry entry);
}
