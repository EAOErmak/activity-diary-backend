package com.example.activity_diary.service;

import com.example.activity_diary.entity.DiaryEntry;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface DiaryService {

    List<DiaryEntry> getAll();
    Optional<DiaryEntry> getById(Long id);
    DiaryEntry create(DiaryEntry entry);
    DiaryEntry update(Long id, DiaryEntry entry);
    DiaryEntry getByIdForUser(Long id, UserDetails currentUser);
    void delete(Long id);
    void deleteAll();
    List<DiaryEntry> getByStatus(String status);
    List<DiaryEntry> getByUserId(Long userId);
}
