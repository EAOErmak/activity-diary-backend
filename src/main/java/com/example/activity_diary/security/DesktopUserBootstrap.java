package com.example.activity_diary.security;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.UserAccount;
import com.example.activity_diary.entity.enums.ProviderType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.repository.UserAccountRepository;
import com.example.activity_diary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("desktop")
@RequiredArgsConstructor
public class DesktopUserBootstrap implements ApplicationRunner {

    private static final String PLACEHOLDER_PASSWORD = "desktop-local-user-disabled-login";

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final DesktopUserProperties desktopUserProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User user = userAccountRepository
                .findByProviderAndProviderId(ProviderType.LOCAL, desktopUserProperties.getProviderId())
                .map(UserAccount::getUser)
                .orElseGet(this::createOrReuseDesktopUser);

        ensureDesktopAccount(user);
        normalizeDesktopUser(user);
        userRepository.save(user);

        log.info("Desktop user is ready: id={}, username={}", user.getId(), user.getUsername());
    }

    private User createOrReuseDesktopUser() {
        return userRepository.findByUsernameForUpdate(desktopUserProperties.getUsername())
                .orElseGet(() -> User.builder()
                        .username(desktopUserProperties.getUsername())
                        .fullName(desktopUserProperties.getFullName())
                        .enabled(true)
                        .role(Role.ADMIN)
                        .build());
    }

    private void ensureDesktopAccount(User user) {
        boolean hasDesktopAccount = user.getAccounts().stream()
                .anyMatch(account ->
                        account.getProvider() == ProviderType.LOCAL
                                && desktopUserProperties.getProviderId().equals(account.getProviderId())
                );

        if (hasDesktopAccount) {
            return;
        }

        user.getAccounts().add(UserAccount.builder()
                .user(user)
                .provider(ProviderType.LOCAL)
                .providerId(desktopUserProperties.getProviderId())
                .passwordHash(passwordEncoder.encode(PLACEHOLDER_PASSWORD))
                .build());
    }

    private void normalizeDesktopUser(User user) {
        user.enable();
        user.unlock();
        user.changeRole(Role.ADMIN);

        if (user.getFullName() == null || user.getFullName().isBlank()) {
            user.setFullName(desktopUserProperties.getFullName());
        }

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            user.setUsername(desktopUserProperties.getUsername());
        }
    }
}
