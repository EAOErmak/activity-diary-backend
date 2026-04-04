package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.TagUsageAggDto;
import com.example.activity_diary.dto.analytics.TagUsageAggFilterDto;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.repository.tag.TagUsageAggRepository;
import com.example.activity_diary.repository.tag.TagUsageAggRow;
import com.example.activity_diary.service.analytics.TagUsageAggService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static java.time.DayOfWeek.MONDAY;

@Service
@RequiredArgsConstructor
public class TagUsageAggServiceImpl implements TagUsageAggService {

    private final TagUsageAggRepository repo;

    @Override
    @Transactional(readOnly = true)
    public List<TagUsageAggDto> getUsage(Long userId, TagUsageAggFilterDto filter) {
        validateRange(filter.getDateFrom(), filter.getDateTo());

        return repo.findUsageRows(
                        userId,
                        filter.getBucket(),
                        filter.getTagId(),
                        filter.getDateFrom(),
                        filter.getDateTo()
                ).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    @Override
    public void onEntryCreated(DiaryEntry entry) {
        applyEntryDelta(entry, 1);
    }

    @Transactional
    @Override
    public void onEntryDeleted(DiaryEntry entry) {
        applyEntryDelta(entry, -1);
    }

    private void applyEntryDelta(DiaryEntry entry, int direction) {
        if (entry == null) return;
        if (entry.getUser() == null || entry.getUser().getId() == null) return;
        if (entry.getWhenStarted() == null) return;
        if (entry.getTags() == null || entry.getTags().isEmpty()) return;

        Long userId = entry.getUser().getId();
        Instant startedAt = entry.getWhenStarted();

        // у тебя duration уже в минутах
        long durationMinutes = entry.getDuration() == null ? 0L : entry.getDuration().longValue() * direction;

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
            repo.addDelta(userId, tagId, "DAY", day, direction, durationMinutes);
            repo.addDelta(userId, tagId, "WEEK", weekStart, direction, durationMinutes);
            repo.addDelta(userId, tagId, "MONTH", monthStart, direction, durationMinutes);
            repo.addDelta(userId, tagId, "HALF_YEAR", halfYearStart, direction, durationMinutes);
            repo.addDelta(userId, tagId, "YEAR", yearStart, direction, durationMinutes);
        }
    }

    private void validateRange(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new BadRequestException("dateFrom must be before or equal to dateTo");
        }
    }

    private TagUsageAggDto toDto(TagUsageAggRow row) {
        return new TagUsageAggDto(
                row.getTagId(),
                row.getTagName(),
                row.getBucket(),
                row.getBucketStart(),
                row.getUsageCount(),
                row.getTotalDurationMinutes()
        );
    }
}
