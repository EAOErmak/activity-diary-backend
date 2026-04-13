package com.example.activity_diary.service.admin;

import com.example.activity_diary.dto.admin.CreateUserByAdminDto;
import com.example.activity_diary.entity.User;

import java.util.List;

public interface AdminUserService {
    List<User> getAllUsers();
    User updateEnabled(Long userId, boolean enabled);
    User updateLock(Long userId, boolean locked);
    User changeRole(Long userId, String role);
    User createUser(CreateUserByAdminDto dto);
}
