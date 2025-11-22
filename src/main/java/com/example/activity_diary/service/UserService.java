package com.example.activity_diary.service;

import com.example.activity_diary.entity.User;

import java.util.Optional;

public interface UserService {
    User save(User user);
    Optional<User> findByUsername(String email);
    Optional<User> findById(Long id);
}
