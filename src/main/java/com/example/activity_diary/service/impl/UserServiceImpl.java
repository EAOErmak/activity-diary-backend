package com.example.activity_diary.service.impl;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.activity_diary.exception.types.*;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User save(User user) { return userRepository.save(user); }

    @Override
    public Optional<User> findByEmail(String email) { return userRepository.findByEmail(email); }

    @Override
    public Optional<User> findById(Long id) { return userRepository.findById(id); }
}
