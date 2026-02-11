package com.example.activity_diary.service.diary;

import org.springframework.security.core.userdetails.UserDetails;

import com.example.activity_diary.entity.diary.DiaryEntry;

public interface DiaryAccessService {

    Long getUserId(UserDetails currentUser);

    DiaryEntry getEntryForUser(Long id, UserDetails currentUser);
}
