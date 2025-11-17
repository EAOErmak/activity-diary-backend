package com.example.activity_diary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String fullName;
}
