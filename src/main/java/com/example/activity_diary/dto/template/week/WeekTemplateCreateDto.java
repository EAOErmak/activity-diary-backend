package com.example.activity_diary.dto.template.week;

import lombok.Data;
import java.util.List;

@Data
public class WeekTemplateCreateDto {
    String name;
    List<WeekTemplateDayItemDto> items;
}
