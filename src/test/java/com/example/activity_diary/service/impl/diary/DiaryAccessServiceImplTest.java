package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.exception.types.ForbiddenException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.diary.DiaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryAccessServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private UserDetails currentUser;

    @InjectMocks
    private DiaryAccessServiceImpl service;

    @Test
    void getUserId_returnsUserId() {
        User user = userWithId(10L);
        when(currentUser.getUsername()).thenReturn("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        Long userId = service.getUserId(currentUser);

        assertEquals(10L, userId);
    }

    @Test
    void getUserId_userMissing_throwsNotFound() {
        when(currentUser.getUsername()).thenReturn("ghost");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getUserId(currentUser));
    }

    @Test
    void getEntryForUser_entryMissing_throwsNotFound() {
        when(diaryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getEntryForUser(1L, currentUser));
    }

    @Test
    void getEntryForUser_ownerMismatch_throwsForbidden() {
        DiaryEntry entry = DiaryEntry.builder().user(userWithId(20L)).build();
        when(diaryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(currentUser.getUsername()).thenReturn("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(userWithId(10L)));

        assertThrows(ForbiddenException.class, () -> service.getEntryForUser(1L, currentUser));
    }

    @Test
    void getEntryForUser_ownerMatch_returnsEntry() {
        DiaryEntry entry = DiaryEntry.builder().user(userWithId(10L)).build();
        when(diaryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(currentUser.getUsername()).thenReturn("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(userWithId(10L)));

        DiaryEntry result = service.getEntryForUser(1L, currentUser);

        assertSame(entry, result);
    }

    private static User userWithId(Long id) {
        User user = User.builder().username("u").build();
        user.setId(id);
        return user;
    }
}
