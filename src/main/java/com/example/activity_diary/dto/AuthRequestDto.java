package com.example.activity_diary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequestDto {
    @NotBlank(message = "Username is required")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
}
