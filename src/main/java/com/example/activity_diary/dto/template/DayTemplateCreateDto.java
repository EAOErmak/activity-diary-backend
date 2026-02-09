package com.example.activity_diary.dto.template;

import lombok.Data;
import java.util.List;

@Data
public class DayTemplateCreateDto {
    private String name;
    private List<Long> entryTemplateIds;
}