package com.example.activity_diary.dto.diary;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DiaryEntryDto {

    private Long id;

    private Long categoryId;
    private String categoryName;

    private Long subCategoryId;
    private String subCategoryName;

    private Instant whenStarted;
    private Instant whenEnded;
    private Integer duration;

    private Short mood;
    private String description;
    private String status;

    private Long userId;

    private List<EntryMetricResponseDto> metrics;

    private Instant createdAt;
    private Instant updatedAt;
}
