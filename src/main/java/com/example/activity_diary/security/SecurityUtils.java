package com.example.activity_diary.security;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof LightUserDetails lud)) {
            throw new IllegalStateException("Invalid principal type");
        }

        return userRepository.findById(lud.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Long getCurrentUserId() {
        return ((LightUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()).getId();
    }
}

