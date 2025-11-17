package com.example.activity_diary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyCodeRequestDto {
    @NotBlank
    private String email;
    @NotBlank
    private String code; // only used for /verification/confirm
}
