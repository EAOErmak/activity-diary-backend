package com.example.activity_diary.service.impl.user;

import com.example.activity_diary.dto.mapper.UserMapper;
import com.example.activity_diary.dto.user.ChangePasswordRequest;
import com.example.activity_diary.dto.user.ChangeUsernameRequest;
import com.example.activity_diary.dto.user.UpdateProfileRequest;
import com.example.activity_diary.dto.user.UserDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.UserAccount;
import com.example.activity_diary.entity.enums.ProviderType;
import com.example.activity_diary.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
    }

    @Test
    void changePassword_userMissing_throwsUsernameNotFound() {
        ChangePasswordRequest req = changePasswordRequest("old", "new-pass");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.changePassword(req, 1L));
    }

    @Test
    void changePassword_nonLocalAccount_throwsIllegalState() {
        User user = userWithAccount(ProviderType.GOOGLE, "hash");
        ChangePasswordRequest req = changePasswordRequest("old", "new-pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> service.changePassword(req, 1L));
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsIllegalArgument() {
        User user = userWithAccount(ProviderType.LOCAL, "hash");
        ChangePasswordRequest req = changePasswordRequest("wrong", "new-pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.changePassword(req, 1L));
    }

    @Test
    void changePassword_sameAsCurrent_throwsIllegalArgument() {
        User user = userWithAccount(ProviderType.LOCAL, "hash");
        ChangePasswordRequest req = changePasswordRequest("old", "old");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "hash")).thenReturn(true, true);

        assertThrows(IllegalArgumentException.class, () -> service.changePassword(req, 1L));
    }

    @Test
    void changePassword_success_updatesHashAndUnlocksUser() {
        User user = userWithAccount(ProviderType.LOCAL, "hash");
        user.lockUntil(LocalDateTime.now().plusDays(1));
        user.increaseFailed2faAttempts();

        ChangePasswordRequest req = changePasswordRequest("old", "new-pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "hash")).thenReturn(true);
        when(passwordEncoder.matches("new-pass", "hash")).thenReturn(false);
        when(passwordEncoder.encode("new-pass")).thenReturn("encoded");

        service.changePassword(req, 1L);

        UserAccount local = user.getAccounts().get(0);
        assertEquals("encoded", local.getPasswordHash());
        assertFalse(user.isAccountLocked());
        assertNull(user.getLockUntil());
        assertEquals(0, user.getFailed2faAttempts());
    }

    @Test
    void updateProfile_setsFullName() {
        User user = userWithAccount(ProviderType.LOCAL, "hash");
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFullName("John Doe");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        service.updateProfile(req, 1L);

        assertEquals("John Doe", user.getFullName());
    }

    @Test
    void changeUsername_existingUsername_throwsIllegalArgument() {
        ChangeUsernameRequest req = new ChangeUsernameRequest();
        req.setNewUsername("taken");
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.changeUsername(req, 1L));
        verify(userRepository, never()).findById(1L);
    }

    @Test
    void changeUsername_success_updatesUsername() {
        User user = userWithAccount(ProviderType.LOCAL, "hash");
        ChangeUsernameRequest req = new ChangeUsernameRequest();
        req.setNewUsername("new-name");

        when(userRepository.existsByUsername("new-name")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        service.changeUsername(req, 1L);

        assertEquals("new-name", user.getUsername());
    }

    @Test
    void getProfile_mapsDto() {
        User user = userWithAccount(ProviderType.LOCAL, "hash");
        UserDto dto = new UserDto();
        dto.setUsername("mapped");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = service.getProfile(1L);

        assertEquals("mapped", result.getUsername());
    }

    @Test
    void getProfile_missing_throwsUsernameNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.getProfile(1L));
    }

    private static ChangePasswordRequest changePasswordRequest(String current, String next) {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword(current);
        req.setNewPassword(next);
        return req;
    }

    private static User userWithAccount(ProviderType provider, String passwordHash) {
        User user = User.builder().username("user").build();
        UserAccount account = UserAccount.builder()
                .provider(provider)
                .providerId("pid")
                .passwordHash(passwordHash)
                .user(user)
                .build();
        user.getAccounts().add(account);
        return user;
    }
}
