package com.example.activity_diary.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangeUsernameRequest {
    @NotBlank
    @Size(min = 3, max = 64)
    private String newUsername;
}

