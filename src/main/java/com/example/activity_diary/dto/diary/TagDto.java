package com.example.activity_diary.dto.diary;

import com.example.activity_diary.entity.enums.TagStatus;
import lombok.Data;

@Data
public class TagDto {
    Long id;
    String name;
    TagStatus status;
}
