package com.example.activity_diary.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
