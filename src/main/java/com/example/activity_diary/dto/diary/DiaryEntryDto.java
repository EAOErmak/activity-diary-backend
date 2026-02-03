package com.example.activity_diary.dto.diary;

import com.example.activity_diary.dto.diary.metric.EntryMetricResponseDto;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class DiaryEntryDto {

    private Long id;

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
