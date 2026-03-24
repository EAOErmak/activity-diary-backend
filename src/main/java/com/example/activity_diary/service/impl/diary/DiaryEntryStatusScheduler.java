package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.enums.UserSyncEntityType;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.service.sync.UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryEntryStatusScheduler {

    private static final List<EntryStatus> FINISHABLE_STATUSES =
            List.of(EntryStatus.SCHEDULED, EntryStatus.ACTIVE);

    private final DiaryRepository diaryRepository;
    private final UserSyncService userSyncService;

    @Scheduled(cron = "0 * * * * *")
    public void refreshStatuses() {
        Instant now = Instant.now();
        Set<Long> affectedUserIds = new HashSet<>();

        affectedUserIds.addAll(
                diaryRepository.findDistinctUserIdsToActivate(EntryStatus.SCHEDULED, now)
        );
        diaryRepository.activateScheduledEntries(
                EntryStatus.SCHEDULED,
                EntryStatus.ACTIVE,
                now
        );

        affectedUserIds.addAll(
                diaryRepository.findDistinctUserIdsToFinish(FINISHABLE_STATUSES, now)
        );
        diaryRepository.finishExpiredEntries(
                FINISHABLE_STATUSES,
                EntryStatus.FINISHED,
                now
        );

        for (Long userId : affectedUserIds) {
            userSyncService.bump(userId, UserSyncEntityType.DIARY);
        }
    }
}
