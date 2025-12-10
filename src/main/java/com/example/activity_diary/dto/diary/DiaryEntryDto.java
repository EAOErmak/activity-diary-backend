package com.example.activity_diary.dto.diary;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DiaryEntryDto {

    private Long id;

    // ✅ Категория
    private Long categoryId;
    private String categoryName;

    // ✅ Подкатегория
    private Long subCategoryId;
    private String subCategoryName;

    // ✅ Время
    private LocalDateTime whenStarted;
    private LocalDateTime whenEnded;
    private Integer duration;

    // ✅ Состояние
    private Short mood;
    private String description;
    private String status;

    // ✅ Пользователь
    private Long userId;

    // ✅ Метрики
    private List<EntryMetricResponseDto> metrics;

    // ✅ Auditing
    private Instant createdAt;
    private Instant updatedAt;
}
