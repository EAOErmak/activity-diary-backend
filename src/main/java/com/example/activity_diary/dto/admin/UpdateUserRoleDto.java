package com.example.activity_diary.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserRoleDto {
    @NotBlank
    private String role;
}
