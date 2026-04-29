package com.example.activity_diary.platform.desktop.security;

import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.entity.enums.ProviderType;
import com.example.activity_diary.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("desktop")
@RequiredArgsConstructor
public class DesktopCurrentUserProvider implements CurrentUserProvider {

    private final UserAccountRepository userAccountRepository;
    private final DesktopUserProperties desktopUserProperties;

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        return userAccountRepository
                .findUserByProviderAndProviderId(
                        ProviderType.LOCAL,
                        desktopUserProperties.getProviderId()
                )
                .orElseThrow(() -> new IllegalStateException("Desktop user is not initialized"));
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }
}
