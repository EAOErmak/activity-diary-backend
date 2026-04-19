package com.example.activity_diary.dto.admin;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class AdminUserDto {
    private Long id;
    private String username;
    private String fullName;
    private String role;
    private boolean enabled;
    private boolean accountLocked;
    private LocalDateTime lockUntil;
    private int failed2faAttempts;
    private Instant createdAt;
    private Long chatId;
}
