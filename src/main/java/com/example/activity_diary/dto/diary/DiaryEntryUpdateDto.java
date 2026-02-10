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

    private Instant whenStarted;

    private Instant whenEnded;

    @Min(value = 1, message = "mood must be between 1 and 5")
    @Max(value = 5, message = "mood must be between 1 and 5")
    private Short mood;

    @Size(max = 1000, message = "description must not exceed 1000 characters")
    private String description;

    private EntryStatus status;

    @Valid
    @Size(max = 30, message = "You cannot add more than 30 metrics at once")
    private List<EntryMetricUpdateDto> metrics;
}
