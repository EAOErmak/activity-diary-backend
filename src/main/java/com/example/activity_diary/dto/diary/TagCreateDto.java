package com.example.activity_diary.dto.diary;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagCreateDto {

    @NotBlank
    private String name;
}
