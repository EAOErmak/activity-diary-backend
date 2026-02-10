package com.example.activity_diary.service.impl.sync;

import com.example.activity_diary.dto.sync.UserSyncStateResponseDto;
import com.example.activity_diary.entity.UserSyncState;
import com.example.activity_diary.entity.enums.UserSyncEntityType;
import com.example.activity_diary.repository.UserSyncStateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSyncServiceImplTest {

    @Mock
    private UserSyncStateRepository userSyncStateRepository;

    @InjectMocks
    private UserSyncServiceImpl service;

    @Test
    void initUser_savesMissingTypes() {
        UserSyncState existing = new UserSyncState(
                10L,
                UserSyncEntityType.DIARY,
                2L,
                LocalDateTime.now()
        );
        when(userSyncStateRepository.findAllByUserId(10L)).thenReturn(List.of(existing));

        service.initUser(10L);

        int expectedSaves = UserSyncEntityType.values().length - 1;
        verify(userSyncStateRepository, times(expectedSaves)).save(any(UserSyncState.class));
    }

    @Test
    void bump_whenUpdated_doesNotInsert() {
        when(userSyncStateRepository.increment(10L, UserSyncEntityType.PROFILE)).thenReturn(1);

        service.bump(10L, UserSyncEntityType.PROFILE);

        verify(userSyncStateRepository, never()).save(any(UserSyncState.class));
    }

    @Test
    void bump_whenMissing_insertsVersionOne() {
        when(userSyncStateRepository.increment(10L, UserSyncEntityType.SETTINGS)).thenReturn(0);

        service.bump(10L, UserSyncEntityType.SETTINGS);

        ArgumentCaptor<UserSyncState> captor = ArgumentCaptor.forClass(UserSyncState.class);
        verify(userSyncStateRepository).save(captor.capture());

        UserSyncState saved = captor.getValue();
        assertEquals(10L, saved.getUserId());
        assertEquals(UserSyncEntityType.SETTINGS, saved.getEntityType());
        assertEquals(1L, saved.getVersion());
    }

    @Test
    void getState_whenEmpty_initializesThenReturnsMap() {
        List<UserSyncState> afterInit = List.of(
                new UserSyncState(10L, UserSyncEntityType.DIARY, 0L, LocalDateTime.now()),
                new UserSyncState(10L, UserSyncEntityType.SETTINGS, 0L, LocalDateTime.now()),
                new UserSyncState(10L, UserSyncEntityType.GOALS, 0L, LocalDateTime.now()),
                new UserSyncState(10L, UserSyncEntityType.PROFILE, 0L, LocalDateTime.now())
        );
        when(userSyncStateRepository.findAllByUserId(10L))
                .thenReturn(List.of())
                .thenReturn(afterInit);

        Map<UserSyncEntityType, Long> state = service.getState(10L);

        assertEquals(UserSyncEntityType.values().length, state.size());
        assertTrue(state.values().stream().allMatch(v -> v == 0L));
    }

    @Test
    void getStateDto_wrapsMap() {
        when(userSyncStateRepository.findAllByUserId(10L)).thenReturn(List.of(
                new UserSyncState(10L, UserSyncEntityType.DIARY, 3L, LocalDateTime.now())
        ));

        UserSyncStateResponseDto dto = service.getStateDto(10L);

        assertEquals(3L, dto.getState().get(UserSyncEntityType.DIARY));
    }
}
