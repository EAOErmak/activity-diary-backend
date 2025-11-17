package com.example.activity_diary.service;

import com.example.activity_diary.entity.DiaryEntry;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.activity_diary.dto.*;
import com.example.activity_diary.entity.User;

import java.util.List;
import java.util.Optional;

public interface DiaryService {

    List<DiaryEntry> getAll();

    Optional<DiaryEntry> getById(Long id);

    List<DiaryEntry> getByUserId(Long userId);

    List<DiaryEntry> getByStatus(String status);

    DiaryEntry create(DiaryEntryCreateDto dto, User user);

    DiaryEntry update(Long id, DiaryEntryUpdateDto dto, UserDetails currentUser);

    DiaryEntry getByIdForUser(Long id, UserDetails currentUser);

    void delete(Long id, UserDetails currentUser);

    void deleteAll();
}
