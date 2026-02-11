package com.example.activity_diary.dto.template.week;

import lombok.Data;

@Data
public class TemplateDayItemViewDto {
    Long id;
    Long dayTemplateId;
    String dayTemplateName;
    Integer dayOfWeek;
}