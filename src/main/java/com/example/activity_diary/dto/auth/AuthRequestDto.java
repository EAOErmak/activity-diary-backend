package com.example.activity_diary.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRequestDto {

    @NotBlank(message = "email is required")
    @Size(min = 3, max = 64, message = "email must be between 3 and 64 characters")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
