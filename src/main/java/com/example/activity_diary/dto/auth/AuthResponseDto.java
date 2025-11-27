package com.example.activity_diary.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponseDto {
    private String accessToken;
    private String refreshToken; // new
    private String username;
    private Long userId;
    private Boolean twoFactorRequired;
}