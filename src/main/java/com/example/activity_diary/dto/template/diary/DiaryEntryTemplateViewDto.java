package com.example.activity_diary.dto.template.diary;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.example.activity_diary.dto.template.TagBriefDto;

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
