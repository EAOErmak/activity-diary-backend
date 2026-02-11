package com.example.activity_diary.dto.template.day;

import lombok.Data;

@Data
public class TemplateEntryItemViewDto {
    Long id;
    Long entryTemplateId;
    String entryTemplateName;
    Integer position;
}
