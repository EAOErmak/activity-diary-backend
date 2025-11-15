package com.example.activity_diary.service;

import com.example.activity_diary.entity.DiaryEntry;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface DiaryService {

    List<DiaryEntry> getAll();

    Optional<DiaryEntry> getById(Long id);

    List<DiaryEntry> getByUserId(Long userId);

    List<DiaryEntry> getByStatus(String status);

    DiaryEntry create(DiaryEntry entry);

    DiaryEntry update(Long id, DiaryEntry updated, UserDetails currentUser);

    DiaryEntry getByIdForUser(Long id, UserDetails currentUser);

    void delete(Long id, UserDetails currentUser);

    void deleteAll();
}
