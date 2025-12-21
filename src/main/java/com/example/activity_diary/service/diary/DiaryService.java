package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryViewDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface DiaryService {

    // ✅ получение списка записей пользователя
    Slice<DiaryEntryViewDto> getMyEntries(Long userId, Pageable pageable);

    // ✅ получить запись (доступ только к своей)
    DiaryEntryDto getMyEntryById(Long id, Long userId);

    // ✅ создать запись
    DiaryEntryDto create(DiaryEntryCreateDto dto, Long userId);

    // ✅ обновить запись
    DiaryEntryDto update(Long id, DiaryEntryUpdateDto dto, Long userId);

    // ✅ удалить запись (логическое удаление)
    void delete(Long id, Long userId);

    List<DiaryEntryViewDto> getEntriesByDateRange(Long id, LocalDateTime from, LocalDateTime to);

    List<DiaryEntryViewDto> getAllEntries(Long id);
}
