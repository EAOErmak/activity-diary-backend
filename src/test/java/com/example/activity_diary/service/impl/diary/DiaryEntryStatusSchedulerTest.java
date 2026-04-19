package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.enums.EntryStatusPolicy;
import com.example.activity_diary.repository.diary.DiaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DiaryEntryStatusSchedulerTest {

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
        verify(diaryRepository).markExpiredEntriesOverdue(
                eq(EntryStatusPolicy.overdueTransitionSourceStatuses()),
                eq(EntryStatus.OVERDUE),
                any(Instant.class)
        );
    }
}
