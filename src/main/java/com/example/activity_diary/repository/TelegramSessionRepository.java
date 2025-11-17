package com.example.activity_diary.repository;

import com.example.activity_diary.entity.TelegramSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TelegramSessionRepository extends JpaRepository<TelegramSession, Long> {
    int deleteByExpiresAtBefore(LocalDateTime time);
}
