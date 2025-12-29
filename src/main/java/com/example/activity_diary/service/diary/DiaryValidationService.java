package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;

public interface DiaryValidationService {

    void validateCreate(DiaryEntryCreateDto dto);

    void validateUpdate(DiaryEntryUpdateDto dto);
}
