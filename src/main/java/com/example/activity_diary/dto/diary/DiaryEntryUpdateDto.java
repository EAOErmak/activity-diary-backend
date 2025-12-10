package com.example.activity_diary.dto.diary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DiaryEntryUpdateDto {

    // ✅ Категория (было: whatHappenedId)
    @Positive(message = "categoryId must be a positive ID")
    private Long categoryId;

    // ✅ Подкатегория (было: whatId)
    @Positive(message = "subCategoryId must be a positive ID")
    private Long subCategoryId;

    // ✅ Можно менять и на будущее, и на прошлое
    private LocalDateTime whenStarted;

    private LocalDateTime whenEnded;

    // ✅ Настроение (было: howYouWereFeeling)
    @PositiveOrZero(message = "mood must be between 0 and 10")
    @Max(value = 10, message = "mood must be between 0 and 10")
    private Short mood;

    // ✅ Описание (было: anyDescription)
    @Size(max = 1000, message = "description must not exceed 1000 characters")
    private String description;

    // ✅ Метрики (было: whatDidYouDo + ActivityItemUpdateDto)
    @Valid
    @Size(max = 30, message = "You cannot add more than 30 metrics at once")
    private List<EntryMetricUpdateDto> metrics;
}
