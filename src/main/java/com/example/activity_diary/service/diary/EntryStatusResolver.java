package com.example.activity_diary.service.diary;

import com.example.activity_diary.entity.enums.EntryStatus;

import java.time.LocalDateTime;

public interface EntryStatusResolver {
    EntryStatus resolve(LocalDateTime whenStarted, LocalDateTime whenEnded);
}
