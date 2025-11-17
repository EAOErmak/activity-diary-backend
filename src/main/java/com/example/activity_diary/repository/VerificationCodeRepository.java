package com.example.activity_diary.repository;

import com.example.activity_diary.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    void deleteByUserId(Long userId);
}
