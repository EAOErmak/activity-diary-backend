package com.example.activity_diary.dto.template;

import lombok.Data;

@Data
public class TemplateGoalTagDto {
    Long tagId;
    String tagName;
    Integer usageCount;
}
