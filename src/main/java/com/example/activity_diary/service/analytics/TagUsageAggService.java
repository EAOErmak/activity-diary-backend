package com.example.activity_diary.service.analytics;

import com.example.activity_diary.entity.diary.DiaryEntry;

public interface TagUsageAggService {
    void onEntryCreated(DiaryEntry entry);
}
