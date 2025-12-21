package com.example.activity_diary.dto.diary;

import com.example.activity_diary.entity.enums.EntryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DiaryEntryViewDto {
    private Long id;
    private EntryStatus status;
    private LocalDateTime whenStarted;
    private LocalDateTime whenEnded;
    private String categoryName;
    private String subCategoryName;
}

