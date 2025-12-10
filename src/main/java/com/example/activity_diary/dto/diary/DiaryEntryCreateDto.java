package com.example.activity_diary.dto.diary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DiaryEntryCreateDto {

    // ✅ Категория (было: whatHappenedId)
    @NotNull(message = "categoryId cannot be null")
    @Positive(message = "categoryId must be a positive ID")
    private Long categoryId;

    // ✅ Подкатегория (было: whatId)
    @Positive(message = "subCategoryId must be a positive ID")
    private Long subCategoryId;

    // ✅ Время начала
    @NotNull(message = "whenStarted is required")
    private LocalDateTime whenStarted;

    // ✅ Время окончания
    @NotNull(message = "whenEnded is required")
    private LocalDateTime whenEnded;

    // ✅ Настроение (было: howYouWereFeeling)
    @PositiveOrZero(message = "mood must be between 0 and 10")
    @Max(value = 10, message = "mood must be between 0 and 10")
    private Short mood;

    // ✅ Описание (было: anyDescription)
    @Size(max = 1000, message = "description must not exceed 1000 characters")
    private String description;

    // ✅ Метрики (было: whatDidYouDo + ActivityItemCreateDto)
    @Valid
    @Size(max = 30, message = "You cannot add more than 30 metrics at once")
    private List<EntryMetricCreateDto> metrics;
}
