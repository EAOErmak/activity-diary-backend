package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.repository.diary.DiaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DiaryEntryStatusSchedulerTest {

    private static final List<EntryStatus> FINISHABLE_STATUSES =
            List.of(EntryStatus.PLANNED, EntryStatus.ACTIVE);

    @Mock
    private DiaryRepository diaryRepository;

    @InjectMocks
    private DiaryEntryStatusScheduler scheduler;

    @Test
    void refreshStatuses_updatesEntries() {
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
    }
}
