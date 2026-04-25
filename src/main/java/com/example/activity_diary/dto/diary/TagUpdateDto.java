package com.example.activity_diary.dto.diary;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagUpdateDto {

    @NotBlank
    private String name;
}
