package com.example.activity_diary.dto.template;

import lombok.Data;

@Data
public class TemplateEntryItemDto {
    Long id;
    Integer position;

    Long entryTemplateId;  // DiaryEntryTemplate id
    String entryTemplateName; // (опционально, но удобно)
}
