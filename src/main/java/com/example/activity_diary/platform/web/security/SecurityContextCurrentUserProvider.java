package com.example.activity_diary.platform.web.security;

import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof LightUserDetails lightUserDetails) {
            return userRepository.findById(lightUserDetails.getId())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        throw new IllegalStateException("Invalid principal type");
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
