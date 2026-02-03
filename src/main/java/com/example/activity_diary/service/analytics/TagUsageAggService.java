package com.example.activity_diary.service.analytics;

import com.example.activity_diary.entity.DiaryEntry;

public interface TagUsageAggService {
    void onEntryCreated(DiaryEntry entry);
}
