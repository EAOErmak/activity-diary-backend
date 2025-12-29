package com.example.activity_diary.service.impl.user;

import com.example.activity_diary.dto.mapper.UserMapper;
import com.example.activity_diary.dto.user.UpdateProfileRequest;
import com.example.activity_diary.dto.user.UserDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.dto.user.ChangePasswordRequest;
import com.example.activity_diary.dto.user.ChangeUsernameRequest;
import com.example.activity_diary.entity.UserAccount;
import com.example.activity_diary.entity.enums.ProviderType;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserAccount account = user.getAccounts().stream()
                .filter(a -> a.getProvider() == ProviderType.LOCAL)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("Password change allowed only for LOCAL accounts")
                );

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            throw new IllegalArgumentException("Current password is empty");
        }

        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("New password is empty");
        }

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                account.getPasswordHash())
        ) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                account.getPasswordHash())
        ) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        account.setPasswordHash(
                passwordEncoder.encode(request.getNewPassword())
        );

        user.unlock(); // опционально
    }

    @Transactional
    @Override
    public void updateProfile(UpdateProfileRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setFullName(request.getFullName());
    }

    @Transactional
    @Override
    public void changeUsername(ChangeUsernameRequest request, Long userId) {
        if (userRepository.existsByUsername(request.getNewUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setUsername(request.getNewUsername());
    }


    @Override
    public UserDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return userMapper.toDto(user);
    }
}
