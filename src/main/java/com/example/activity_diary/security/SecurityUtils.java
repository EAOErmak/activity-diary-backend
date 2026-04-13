package com.example.activity_diary.security;

import com.example.activity_diary.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final CurrentUserProvider currentUserProvider;

    public User getCurrentUser() {
        return currentUserProvider.getCurrentUser();
    }

    public Long getCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }
}

