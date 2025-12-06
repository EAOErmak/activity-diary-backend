package com.example.activity_diary.repository;

import com.example.activity_diary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByChatId(Long chatId);
    boolean existsByUsername(String username);
    boolean existsByChatId(Long chatId);
    long countByAccountLockedTrue();
    long countByCreatedAtAfter(Instant date);
    @Query("""
        SELECT COUNT(DISTINCT d.user.id)
        FROM DiaryEntry d
        WHERE d.createdAt >= :start AND d.createdAt < :end
    """)
    long countActiveUsersBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


}
