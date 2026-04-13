package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.admin.CreateUserByAdminDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.UserAccount;
import com.example.activity_diary.entity.enums.ProviderType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.UserAccountRepository;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public User createUser(CreateUserByAdminDto dto) {
        String username = normalizeRequiredValue(dto.getUsername(), "Username");
        String providerId = normalizeProviderId(dto.getUsername(), dto.getEmail());
        String fullName = normalizeOptionalValue(dto.getFullName());

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists");
        }

        if (userAccountRepository.existsByProviderAndProviderId(ProviderType.LOCAL, providerId)) {
            throw new BadRequestException("Login already exists");
        }

        User user = User.builder()
                .username(username)
                .fullName(fullName)
                .role(dto.getRole())
                .enabled(true)
                .build();

        UserAccount account = UserAccount.builder()
                .user(user)
                .provider(ProviderType.LOCAL)
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .providerId(providerId)
                .build();

        user.getAccounts().add(account);

        User savedUser = userRepository.save(user);
        log.info("Admin user service created user: id={}, username={}, providerId={}, role={}",
                savedUser.getId(), savedUser.getUsername(), providerId, savedUser.getRole());
        return savedUser;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateEnabled(Long userId, boolean enabled) {
        User user = getUser(userId);

        if (enabled) {
            user.enable();
        } else {
            user.disable();
        }

        User savedUser = userRepository.save(user);
        log.info("Admin user service updated enabled state: id={}, enabled={}", savedUser.getId(), savedUser.isEnabled());
        return savedUser;
    }

    @Override
    public User updateLock(Long userId, boolean locked) {
        User user = getUser(userId);

        if (locked) {
            user.lockUntil(LocalDateTime.now().plusYears(100));
        } else {
            user.unlock();
        }

        User savedUser = userRepository.save(user);
        log.info("Admin user service updated lock state: id={}, locked={}", savedUser.getId(), savedUser.isCurrentlyLocked());
        return savedUser;
    }

    @Override
    public User changeRole(Long userId, String role) {
        Role newRole;
        try {
            newRole = Role.valueOf(role.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid role: " + role);
        }

        User user = getUser(userId);

        user.changeRole(newRole);
        User savedUser = userRepository.save(user);
        log.info("Admin user service updated role: id={}, role={}", savedUser.getId(), savedUser.getRole());
        return savedUser;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String normalizeProviderId(String username, String email) {
        String normalizedEmail = normalizeOptionalValue(email);
        if (normalizedEmail != null) {
            return normalizedEmail.toLowerCase(Locale.ROOT);
        }
        return normalizeRequiredValue(username, "Username");
    }

    private String normalizeRequiredValue(String value, String fieldName) {
        String normalizedValue = normalizeOptionalValue(value);
        if (normalizedValue == null) {
            throw new BadRequestException(fieldName + " is required");
        }
        return normalizedValue.toLowerCase(Locale.ROOT);
    }

    private String normalizeOptionalValue(String value) {
        if (value == null) {
            return null;
        }

        String normalizedValue = value.trim();
        return normalizedValue.isEmpty() ? null : normalizedValue;
    }
}
