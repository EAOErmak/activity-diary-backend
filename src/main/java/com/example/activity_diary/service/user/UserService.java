package com.example.activity_diary.service.user;

import com.example.activity_diary.dto.user.ChangePasswordRequest;
import com.example.activity_diary.dto.user.ChangeUsernameRequest;
import com.example.activity_diary.dto.user.UpdateProfileRequest;
import com.example.activity_diary.dto.user.UserDto;
import com.example.activity_diary.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserService {

    UserDto getProfile(Long userId);

    @Transactional
    void updateProfile(UpdateProfileRequest request, Long userId);

    @Transactional
    void changePassword(ChangePasswordRequest request, Long userId);

    @Transactional
    void changeUsername(ChangeUsernameRequest request, Long userId);

}

