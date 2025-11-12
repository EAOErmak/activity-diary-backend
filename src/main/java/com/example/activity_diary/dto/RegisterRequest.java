package com.example.activity_diary.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
}
