package com.example.activity_diary.entity.enums;

public enum SessionState {
    NONE,               // Default state, no active operation
    WAIT_EMAIL,         // User must provide email
    WAIT_CODE           // User must provide 6-digit code
}
