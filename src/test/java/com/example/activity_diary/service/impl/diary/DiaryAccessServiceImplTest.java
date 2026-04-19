package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.exception.types.ForbiddenException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.core.usercontext.CurrentUserProvider;
import com.example.activity_diary.repository.diary.DiaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryAccessServiceImplTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private DiaryRepository diaryRepository;

    @InjectMocks
    private DiaryAccessServiceImpl service;

    @Test
    void getCurrentUserId_returnsUserId() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);

        Long userId = service.getCurrentUserId();

        assertEquals(10L, userId);
    }

    @Test
    void getEntryForCurrentUser_entryMissing_throwsNotFound() {
        when(diaryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getEntryForCurrentUser(1L));
    }

    @Test
    void getEntryForCurrentUser_ownerMismatch_throwsForbidden() {
        DiaryEntry entry = DiaryEntry.builder()
                .user(com.example.activity_diary.entity.User.builder().build())
                .build();
        entry.getUser().setId(20L);
        when(diaryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);

        assertThrows(ForbiddenException.class, () -> service.getEntryForCurrentUser(1L));
    }

    @Test
    void getEntryForCurrentUser_ownerMatch_returnsEntry() {
        DiaryEntry entry = DiaryEntry.builder()
                .user(com.example.activity_diary.entity.User.builder().build())
                .build();
        entry.getUser().setId(10L);
        when(diaryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);

        DiaryEntry result = service.getEntryForCurrentUser(1L);

        assertSame(entry, result);
    }
}
