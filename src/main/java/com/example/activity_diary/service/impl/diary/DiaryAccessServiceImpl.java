package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.exception.types.ForbiddenException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.DiaryRepository;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.service.diary.DiaryAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiaryAccessServiceImpl implements DiaryAccessService {

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;

    @Override
    public Long getUserId(UserDetails currentUser) {
        String username = currentUser.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return user.getId();
    }

    @Override
    public DiaryEntry getEntryForUser(Long id, UserDetails currentUser) {

        DiaryEntry entry = diaryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Diary entry not found"));

        Long userId = getUserId(currentUser);

        if (!entry.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Access denied to this diary entry");
        }

        return entry;
    }
}
