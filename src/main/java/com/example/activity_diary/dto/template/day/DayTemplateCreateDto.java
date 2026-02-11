package com.example.activity_diary.dto.template.day;

import lombok.Data;
import java.util.List;

@Data
public class DayTemplateCreateDto {
    String name;
    List<TemplateEntryItemCreateDto> items;
}