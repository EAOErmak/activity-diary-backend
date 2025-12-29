package com.example.activity_diary.service.admin;

import com.example.activity_diary.dto.admin.CreateUserByAdminDto;
import com.example.activity_diary.entity.User;

import java.util.List;

public interface AdminUserService {
    List<User> getAllUsers();
    void blockUser(Long userId);
    void unblockUser(Long userId);
    void changeRole(Long userId, String role);
    void createUser(CreateUserByAdminDto dto);
}
