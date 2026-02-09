package com.example.activity_diary.dto.template;

import lombok.Data;

@Data
public class TemplateDayItemDto {
    Long id;
    Integer position;

    Long dayTemplateId;     // Template id (DAY)
    String dayTemplateName; // (опционально)
}
