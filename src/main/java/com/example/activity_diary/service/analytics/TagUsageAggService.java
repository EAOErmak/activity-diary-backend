package com.example.activity_diary.service.analytics;

import com.example.activity_diary.dto.analytics.TagUsageAggDto;
import com.example.activity_diary.dto.analytics.TagUsageAggFilterDto;
import com.example.activity_diary.entity.diary.DiaryEntry;

import java.util.List;

public interface TagUsageAggService {
    List<TagUsageAggDto> getUsage(Long userId, TagUsageAggFilterDto filter);

    void onEntryCreated(DiaryEntry entry);
}
