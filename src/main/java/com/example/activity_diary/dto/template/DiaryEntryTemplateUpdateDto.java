package com.example.activity_diary.dto.template;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class DiaryEntryTemplateUpdateDto {
    @Size(max = 120)
    String name;

    Short mood;

    @Size(max = 1000)
    String description;

    @Valid
    List<EntryTemplateMetricUpsertDto> metrics;
}
