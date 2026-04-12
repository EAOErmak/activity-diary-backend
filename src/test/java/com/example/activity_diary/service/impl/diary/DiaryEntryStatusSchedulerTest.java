package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.enums.UserSyncEntityType;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.service.sync.UserSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryEntryStatusSchedulerTest {

    private static final List<EntryStatus> FINISHABLE_STATUSES =
            List.of(EntryStatus.PLANNED, EntryStatus.ACTIVE);

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private UserSyncService userSyncService;

    @InjectMocks
    private DiaryEntryStatusScheduler scheduler;

    @Test
    void refreshStatuses_updatesEntriesAndBumpsDistinctUsers() {
        when(diaryRepository.findDistinctUserIdsToActivate(eq(EntryStatus.PLANNED), any(Instant.class)))
                .thenReturn(List.of(1L, 2L));
        when(diaryRepository.findDistinctUserIdsToFinish(eq(FINISHABLE_STATUSES), any(Instant.class)))
                .thenReturn(List.of(2L, 3L));

        scheduler.refreshStatuses();

        verify(diaryRepository).activateScheduledEntries(
                eq(EntryStatus.PLANNED),
                eq(EntryStatus.ACTIVE),
                any(Instant.class)
        );
        verify(diaryRepository).finishExpiredEntries(
                eq(FINISHABLE_STATUSES),
                eq(EntryStatus.FINISHED),
                any(Instant.class)
        );
        verify(userSyncService).bump(1L, UserSyncEntityType.DIARY);
        verify(userSyncService).bump(2L, UserSyncEntityType.DIARY);
        verify(userSyncService).bump(3L, UserSyncEntityType.DIARY);
        verify(userSyncService, times(3)).bump(anyLong(), eq(UserSyncEntityType.DIARY));
    }

    @Test
    void refreshStatuses_withoutChanges_doesNotBumpSync() {
        when(diaryRepository.findDistinctUserIdsToActivate(eq(EntryStatus.PLANNED), any(Instant.class)))
                .thenReturn(List.of());
        when(diaryRepository.findDistinctUserIdsToFinish(eq(FINISHABLE_STATUSES), any(Instant.class)))
                .thenReturn(List.of());

        scheduler.refreshStatuses();

        verify(diaryRepository).activateScheduledEntries(
                eq(EntryStatus.PLANNED),
                eq(EntryStatus.ACTIVE),
                any(Instant.class)
        );
        verify(diaryRepository).finishExpiredEntries(
                eq(FINISHABLE_STATUSES),
                eq(EntryStatus.FINISHED),
                any(Instant.class)
        );
        verify(userSyncService, never()).bump(anyLong(), eq(UserSyncEntityType.DIARY));
    }
}
