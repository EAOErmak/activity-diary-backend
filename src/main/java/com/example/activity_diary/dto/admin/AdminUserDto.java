// src/main/java/com/example/activity_diary/dto/admin/AdminUserDto.java
package com.example.activity_diary.dto.admin;

import lombok.Data;

import java.time.Instant;

@Data
public class AdminUserDto {
    private Long id;
    private String username;
    private String fullName;
    private String role;        // ADMIN / USER
    private Boolean enabled;
    private Boolean accountLocked;
    private Instant createdAt;
    private Long chatId;
}
