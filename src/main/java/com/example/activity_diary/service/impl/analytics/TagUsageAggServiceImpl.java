package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.Tag;
import com.example.activity_diary.repository.TagUsageAggRepository;
import com.example.activity_diary.service.analytics.TagUsageAggService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

import static java.time.DayOfWeek.MONDAY;

@Service
@RequiredArgsConstructor
public class TagUsageAggServiceImpl implements TagUsageAggService {

    private final TagUsageAggRepository repo;

    @Transactional
    @Override
    public void onEntryCreated(DiaryEntry entry) {
        if (entry == null) return;
        if (entry.getUser() == null || entry.getUser().getId() == null) return;
        if (entry.getWhenStarted() == null) return;
        if (entry.getTags() == null || entry.getTags().isEmpty()) return;

        Long userId = entry.getUser().getId();
        Instant startedAt = entry.getWhenStarted();

        // у тебя duration уже в минутах
        long durationMinutes = entry.getDuration() == null ? 0L : entry.getDuration().longValue();

        // UTC
        LocalDate day = startedAt.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate weekStart = day.with(TemporalAdjusters.previousOrSame(MONDAY));
        LocalDate monthStart = day.withDayOfMonth(1);

        int year = day.getYear();
        LocalDate yearStart = LocalDate.of(year, 1, 1);

        LocalDate halfYearStart = (day.getMonthValue() <= 6)
                ? LocalDate.of(year, 1, 1)
                : LocalDate.of(year, 7, 1);

        for (Tag tag : entry.getTags()) {
            if (tag == null || tag.getId() == null) continue;

            Long tagId = tag.getId();

            // +1 запись и +duration минут в 5 бакетов
            repo.addDelta(userId, tagId, "DAY", day, 1, durationMinutes);
            repo.addDelta(userId, tagId, "WEEK", weekStart, 1, durationMinutes);
            repo.addDelta(userId, tagId, "MONTH", monthStart, 1, durationMinutes);
            repo.addDelta(userId, tagId, "HALF_YEAR", halfYearStart, 1, durationMinutes);
            repo.addDelta(userId, tagId, "YEAR", yearStart, 1, durationMinutes);
        }
    }
}
