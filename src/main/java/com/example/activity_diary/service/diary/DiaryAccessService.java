package com.example.activity_diary.service.diary;

import com.example.activity_diary.entity.DiaryEntry;
import org.springframework.security.core.userdetails.UserDetails;

public interface DiaryAccessService {

    Long getUserId(UserDetails currentUser);

    DiaryEntry getEntryForUser(Long id, UserDetails currentUser);
}
