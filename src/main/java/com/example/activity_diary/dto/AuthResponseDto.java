package com.example.activity_diary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {
    private String accessToken;
    private String refreshToken; // new
    private String username;
    private Long userId;
}