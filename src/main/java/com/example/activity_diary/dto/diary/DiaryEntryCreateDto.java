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

    @NotNull(message = "categoryId cannot be null")
    @Positive(message = "categoryId must be a positive ID")
    private Long categoryId;

    @Positive(message = "subCategoryId must be a positive ID")
    private Long subCategoryId;

    @NotNull(message = "whenStarted is required")
    private LocalDateTime whenStarted;

    @NotNull(message = "whenEnded is required")
    private LocalDateTime whenEnded;

    @PositiveOrZero(message = "mood must be between 0 and 10")
    @Max(value = 10, message = "mood must be between 0 and 10")
    private Short mood;

    @Size(max = 1000, message = "description must not exceed 1000 characters")
    private String description;

    @Valid
    @Size(max = 30, message = "You cannot add more than 30 metrics at once")
    private List<EntryMetricCreateDto> metrics;
}
