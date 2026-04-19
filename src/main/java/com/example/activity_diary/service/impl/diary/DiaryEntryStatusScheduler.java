package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.enums.EntryStatusPolicy;
import com.example.activity_diary.repository.diary.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
@Service
@RequiredArgsConstructor
@Transactional
public class DiaryEntryStatusScheduler {

    private final DiaryRepository diaryRepository;

    @Scheduled(cron = "0 * * * * *")
    public void refreshStatuses() {
        Instant now = Instant.now();

        diaryRepository.activateScheduledEntries(
                EntryStatus.PLANNED,
                EntryStatus.ACTIVE,
                now
        );

        diaryRepository.markExpiredEntriesOverdue(
                EntryStatusPolicy.overdueTransitionSourceStatuses(),
                EntryStatus.OVERDUE,
                now
        );
    }
}
