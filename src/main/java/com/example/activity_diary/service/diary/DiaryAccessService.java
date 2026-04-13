package com.example.activity_diary.service.diary;

import com.example.activity_diary.entity.diary.DiaryEntry;

public interface DiaryAccessService {

    Long getCurrentUserId();

    DiaryEntry getEntryForCurrentUser(Long id);
}
