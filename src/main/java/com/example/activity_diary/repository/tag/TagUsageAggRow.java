package com.example.activity_diary.repository.tag;

import com.example.activity_diary.entity.enums.TagUsageBucket;

import java.time.LocalDate;

public interface TagUsageAggRow {
    Long getTagId();

    String getTagName();

    TagUsageBucket getBucket();

    LocalDate getBucketStart();

    int getUsageCount();

    long getTotalDurationMinutes();
}
