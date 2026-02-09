package com.example.activity_diary.dto.template;

import com.example.activity_diary.entity.enums.TemplateType;
import lombok.Data;

import java.time.Instant;

@Data
public class TemplateListItemDto {
    Long id;
    TemplateType type;
    String name;
    Instant updatedAt;

    // counts (для UI списка)
    Integer dayItemsCount;   // если DAY
    Integer weekItemsCount;  // если WEEK
    Integer goalsTagsCount;
    Integer goalsMetricsCount;
}
