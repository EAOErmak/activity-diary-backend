package com.example.activity_diary.dto.diary;

import com.example.activity_diary.dto.diary.metric.EntryMetricUpdateDto;
import com.example.activity_diary.entity.enums.EntryStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class DiaryEntryUpdateDto {

    @Positive(message = "categoryId must be a positive ID")
    private Long categoryId;

    @Positive(message = "subCategoryId must be a positive ID")
    private Long subCategoryId;

    private Instant whenStarted;

    private Instant whenEnded;

    @PositiveOrZero(message = "mood must be between 0 and 10")
    @Max(value = 10, message = "mood must be between 0 and 10")
    private Short mood;

    @Size(max = 1000, message = "description must not exceed 1000 characters")
    private String description;

    private EntryStatus status;

    @Valid
    @Size(max = 30, message = "You cannot add more than 30 metrics at once")
    private List<EntryMetricUpdateDto> metrics;

    @Size(max = 100, message = "tags must not exceed 1000 characters")
    List<String> tags;
}
