package com.example.activity_diary.dto;

import lombok.Data;

@Data
public class VerifyCodeDto {
    private String email;
    private String code;
}
