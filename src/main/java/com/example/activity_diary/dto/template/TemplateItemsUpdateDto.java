package com.example.activity_diary.dto.template;

import lombok.Data;
import java.util.List;

@Data
public class TemplateItemsUpdateDto {
    private List<Long> ids; // entryTemplateIds для DAY, dayTemplateIds для WEEK
}