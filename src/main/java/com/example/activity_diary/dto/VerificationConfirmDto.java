package com.example.activity_diary.dto;

import lombok.Data;

@Data
public class VerificationConfirmDto {
    private String email;
    private String code;
}