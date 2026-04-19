package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.exception.types.ForbiddenException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.service.diary.DiaryAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiaryAccessServiceImpl implements DiaryAccessService {

    private final CurrentUserProvider currentUserProvider;
    private final DiaryRepository diaryRepository;

    @Override
    public Long getCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }

    @Override
    public DiaryEntry getEntryForCurrentUser(Long id) {

        DiaryEntry entry = diaryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Diary entry not found"));

        Long userId = getCurrentUserId();

        if (!entry.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Access denied to this diary entry");
        }

        return entry;
    }
}
