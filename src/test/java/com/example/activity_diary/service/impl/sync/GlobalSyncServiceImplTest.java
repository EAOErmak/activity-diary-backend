package com.example.activity_diary.service.impl.sync;

import com.example.activity_diary.dto.sync.GlobalSyncStateResponseDto;
import com.example.activity_diary.entity.GlobalSyncState;
import com.example.activity_diary.entity.enums.GlobalSyncEntityType;
import com.example.activity_diary.repository.GlobalSyncStateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalSyncServiceImplTest {

    @Mock
    private GlobalSyncStateRepository repository;

    @InjectMocks
    private GlobalSyncServiceImpl service;

    @Test
    void initIfNeeded_createsMissingTypes() {
        Map<GlobalSyncEntityType, GlobalSyncState> store = new EnumMap<>(GlobalSyncEntityType.class);

        when(repository.findById(any(GlobalSyncEntityType.class)))
                .thenAnswer(invocation -> {
                    GlobalSyncEntityType type = invocation.getArgument(0);
                    return Optional.ofNullable(store.get(type));
                });

        when(repository.save(any(GlobalSyncState.class)))
                .thenAnswer(invocation -> {
                    GlobalSyncState saved = invocation.getArgument(0);
                    store.put(saved.getEntityType(), saved);
                    return saved;
                });

        service.initIfNeeded();

        assertEquals(GlobalSyncEntityType.values().length, store.size());
        verify(repository, times(GlobalSyncEntityType.values().length)).save(any(GlobalSyncState.class));
    }

    @Test
    void bump_whenUpdated_doesNotInsert() {
        when(repository.increment(GlobalSyncEntityType.TAG)).thenReturn(1);

        service.bump(GlobalSyncEntityType.TAG);

        verify(repository, never()).save(any(GlobalSyncState.class));
    }

    @Test
    void bump_whenMissing_insertsVersionOne() {
        when(repository.increment(GlobalSyncEntityType.DICTIONARY)).thenReturn(0);

        service.bump(GlobalSyncEntityType.DICTIONARY);

        ArgumentCaptor<GlobalSyncState> captor = ArgumentCaptor.forClass(GlobalSyncState.class);
        verify(repository).save(captor.capture());

        GlobalSyncState saved = captor.getValue();
        assertEquals(GlobalSyncEntityType.DICTIONARY, saved.getEntityType());
        assertEquals(1L, saved.getVersion());
    }

    @Test
    void getState_returnsVersionMap() {
        Map<GlobalSyncEntityType, GlobalSyncState> store = new EnumMap<>(GlobalSyncEntityType.class);
        for (GlobalSyncEntityType type : GlobalSyncEntityType.values()) {
            store.put(type, new GlobalSyncState(type, 5L, LocalDateTime.now()));
        }

        when(repository.findById(any(GlobalSyncEntityType.class)))
                .thenAnswer(invocation -> Optional.of(store.get(invocation.getArgument(0))));
        when(repository.findAll()).thenReturn(store.values().stream().toList());

        Map<GlobalSyncEntityType, Long> state = service.getState();

        assertEquals(GlobalSyncEntityType.values().length, state.size());
        assertTrue(state.values().stream().allMatch(v -> v == 5L));
    }

    @Test
    void getStateDto_wrapsMap() {
        Map<GlobalSyncEntityType, GlobalSyncState> store = new EnumMap<>(GlobalSyncEntityType.class);
        store.put(GlobalSyncEntityType.TAG, new GlobalSyncState(GlobalSyncEntityType.TAG, 9L, LocalDateTime.now()));

        when(repository.findById(any(GlobalSyncEntityType.class)))
                .thenAnswer(invocation -> Optional.ofNullable(store.get(invocation.getArgument(0))));
        when(repository.save(any(GlobalSyncState.class)))
                .thenAnswer(invocation -> {
                    GlobalSyncState saved = invocation.getArgument(0);
                    store.put(saved.getEntityType(), saved);
                    return saved;
                });
        when(repository.findAll()).thenReturn(store.values().stream().toList());

        GlobalSyncStateResponseDto dto = service.getStateDto();

        assertEquals(9L, dto.getState().get(GlobalSyncEntityType.TAG));
    }
}
