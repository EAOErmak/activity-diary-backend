package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.admin.CreateUserByAdminDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.UserAccount;
import com.example.activity_diary.entity.enums.ProviderType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public void createUser(CreateUserByAdminDto dto) {

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("User already exists");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .fullName(dto.getFullName())
                .role(dto.getRole())
                .enabled(true)
                .build();

        UserAccount account = UserAccount.builder()
                .user(user)
                .provider(ProviderType.LOCAL)
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .providerId(dto.getEmail())
                .build();

        user.getAccounts().add(account);

        userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));


        user.lockUntil(LocalDateTime.now().plusYears(100));
        userRepository.save(user);
    }

    @Override
    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.unlock();
        userRepository.save(user);
    }

    @Override
    public void changeRole(Long userId, String role) {

        Role newRole;
        try {
            newRole = Role.valueOf(role.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid role: " + role);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.changeRole(newRole);
        userRepository.save(user);
    }
}
