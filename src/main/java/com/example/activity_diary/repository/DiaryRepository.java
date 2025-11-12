package com.example.activity_diary.repository;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.enums.EntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryRepository extends JpaRepository<DiaryEntry, Long> {
    List<DiaryEntry> findByStatus(EntryStatus status);
    List<DiaryEntry> findByUserId(Long userId);
    List<DiaryEntry> findByUserIdAndStatus(Long userId, EntryStatus status);
}
