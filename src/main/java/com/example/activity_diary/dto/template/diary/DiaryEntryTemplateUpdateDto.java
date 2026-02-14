package com.example.activity_diary.dto.template.diary;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class DiaryEntryTemplateUpdateDto {

    @Size(max = 120)
    String name;

    Short mood;

    @Size(max = 1000)
    String description;

    @JsonFormat(pattern = "HH:mm")
    LocalTime timeStart;

    @JsonFormat(pattern = "HH:mm")
    LocalTime timeEnd;

    @Valid
    List<EntryTemplateMetricUpsertDto> metrics;
}
