package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryViewDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.entity.enums.DiaryEntryCreateMode;
import com.example.activity_diary.entity.enums.EntryStatus;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface DiaryService {

    Slice<DiaryEntryViewDto> getMyEntries(Long userId, Pageable pageable);

    Slice<DiaryEntryViewDto> getMyEntriesFiltered(
            Long userId,
            EntryStatus status,
            List<String> tags,
            Instant from,
            Instant to,
            Pageable pageable
    );

    DiaryEntryDto getMyEntryById(Long id, Long userId);

    DiaryEntryDto create(DiaryEntryCreateDto dto, Long userId, DiaryEntryCreateMode mode);

    default DiaryEntryDto create(DiaryEntryCreateDto dto, Long userId) {
        return create(dto, userId, DiaryEntryCreateMode.NORMAL);
    }

    List<DiaryEntryDto> createAll(List<DiaryEntryCreateDto> dtos, Long userId, DiaryEntryCreateMode mode);

    DiaryEntryDto update(Long id, DiaryEntryUpdateDto dto, Long userId);

    void delete(Long id, Long userId);

    List<DiaryEntryViewDto> getEntriesByDateRange(Long id, LocalDateTime from, LocalDateTime to);

    List<DiaryEntryViewDto> getAllEntries(Long id);
}
