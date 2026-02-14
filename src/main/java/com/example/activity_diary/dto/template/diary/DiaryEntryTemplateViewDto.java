package com.example.activity_diary.dto.template.diary;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
public class DiaryEntryTemplateViewDto {

    Long id;
    String name;
    Short mood;
    String description;

    LocalTime timeStart;
    LocalTime timeEnd;

    List<EntryTemplateMetricViewDto> metrics;

    Instant createdAt;
    Instant updatedAt;
}
