package com.example.activity_diary.dto.template.diary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class DiaryEntryTemplateCreateDto {
    @NotBlank
    @Size(max = 120)
    String name;

    Short mood;

    @Size(max = 1000)
    String description;

    @Valid
    List<EntryTemplateMetricUpsertDto> metrics;
}
