package com.example.activity_diary.service.analytics;

import com.example.activity_diary.entity.DiaryEntry;

public interface MetricUsageAggService {
    void onEntryCreated(DiaryEntry entry);
}