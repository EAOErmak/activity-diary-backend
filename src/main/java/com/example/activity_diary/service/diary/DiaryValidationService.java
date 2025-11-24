package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;

public interface DiaryValidationService {

    void validateCreate(DiaryEntryCreateDto dto);
}
