package com.example.activity_diary.dto;

import lombok.Data;

@Data
public class VerifyCodeRequest {
    private Long userId;
    private String code;
}
