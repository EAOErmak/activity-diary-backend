package com.example.activity_diary.service.analytics;

import com.example.activity_diary.entity.diary.DiaryEntry;

public interface MetricUsageAggService {
    void onEntryCreated(DiaryEntry entry);
}