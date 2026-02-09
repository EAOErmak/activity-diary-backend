package com.example.activity_diary.dto.template;

import lombok.Data;
import java.util.List;

@Data
public class WeekTemplateCreateDto {
    private String name;
    private List<Long> dayTemplateIds;
}