package com.example.activity_diary.repository;

import com.example.activity_diary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByChatId(Long chatId);
    boolean existsByUsername(String username);
    boolean existsByChatId(Long chatId);
}
