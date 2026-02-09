package com.example.activity_diary.dto.template;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class DiaryEntryTemplateViewDto {
    Long id;
    String name;
    Short mood;
    String description;
    Set<TagBriefDto> tags;
    List<EntryTemplateMetricViewDto> metrics;
    Instant createdAt;
    Instant updatedAt;
}
