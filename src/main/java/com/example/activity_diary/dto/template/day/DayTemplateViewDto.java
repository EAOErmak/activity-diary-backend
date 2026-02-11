package com.example.activity_diary.dto.template.day;

import lombok.Data;
import java.util.List;

@Data
public class DayTemplateViewDto {
    Long id;
    String name;
    List<TemplateEntryItemViewDto> items;
}
