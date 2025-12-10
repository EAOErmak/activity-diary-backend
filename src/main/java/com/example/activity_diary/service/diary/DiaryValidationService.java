package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;

public interface DiaryValidationService {

    // ✅ валидация перед созданием
    void validateCreate(DiaryEntryCreateDto dto);

    // ✅ валидация перед обновлением
    void validateUpdate(DiaryEntryUpdateDto dto);
}
