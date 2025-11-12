package com.example.activity_diary.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthRequest {
    private String email;
    private String password;
}
