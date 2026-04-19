package com.example.activity_diary.service.diary;

import com.example.activity_diary.entity.enums.EntryStatus;

import java.time.Instant;

public interface EntryStatusResolver {
    EntryStatus resolve(Instant whenStarted, Instant whenEnded, Instant now);
}
