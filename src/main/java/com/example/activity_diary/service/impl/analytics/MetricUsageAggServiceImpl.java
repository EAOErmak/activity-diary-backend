package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.MetricUsageAggDto;
import com.example.activity_diary.dto.analytics.MetricUsageAggFilterDto;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.diary.EntryMetricValue;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.repository.diary.MetricUsageAggRepository;
import com.example.activity_diary.repository.diary.MetricUsageAggRow;
import com.example.activity_diary.service.analytics.MetricUsageAggService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.time.DayOfWeek.MONDAY;

@Service
@RequiredArgsConstructor
public class MetricUsageAggServiceImpl implements MetricUsageAggService {

    private final MetricUsageAggRepository repo;

    @Override
    @Transactional(readOnly = true)
    public List<MetricUsageAggDto> getUsage(Long userId, MetricUsageAggFilterDto filter) {
        validateRange(filter.getDateFrom(), filter.getDateTo());

        return repo.findUsageRows(
                        userId,
                        filter.getBucket(),
                        filter.getMetricTypeId(),
                        filter.getUnitId(),
                        filter.getDateFrom(),
                        filter.getDateTo()
                ).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Агрегируем метрики по ключу (metricTypeId, unitId):
     * sumInc = сумма value внутри записи
     * countInc = сколько значений сложили (для среднего)
     *
     * Потом эту дельту пишем в 5 бакетов по whenStarted (UTC).
     */
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
        if (entry.getMetrics() == null || entry.getMetrics().isEmpty()) return;

        Long userId = entry.getUser().getId();

        // 1) bucket_start по UTC от whenStarted
        LocalDate day = entry.getWhenStarted().atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate weekStart = day.with(TemporalAdjusters.previousOrSame(MONDAY));
        LocalDate monthStart = day.withDayOfMonth(1);
        int year = day.getYear();
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate halfYearStart = (day.getMonthValue() <= 6)
                ? LocalDate.of(year, 1, 1)
                : LocalDate.of(year, 7, 1);

        // 2) собрать дельты по ключу (metricTypeId, unitId) внутри ОДНОЙ записи
        Map<Key, Delta> deltas = new HashMap<>();

        for (EntryMetric metric : entry.getMetrics()) {
            if (metric == null) continue;
            if (metric.getMetricType() == null || metric.getMetricType().getId() == null) continue;

            Long metricTypeId = metric.getMetricType().getId();

            for (EntryMetricValue v : metric.getValues()) {
                if (v == null) continue;
                if (v.getUnit() == null || v.getUnit().getId() == null) continue;
                if (v.getValue() == null) continue;

                Long unitId = v.getUnit().getId();
                int value = v.getValue();

                Key key = new Key(metricTypeId, unitId);
                Delta d = deltas.computeIfAbsent(key, k -> new Delta());
                d.sum += (long) value * direction;
                d.count += direction;
            }
        }

        if (deltas.isEmpty()) return;

        // 3) применить дельты в 5 бакетов
        for (Map.Entry<Key, Delta> e : deltas.entrySet()) {
            Key key = e.getKey();
            Delta d = e.getValue();

            repo.addDelta(userId, key.metricTypeId, key.unitId, "DAY", day, d.sum, d.count);
            repo.addDelta(userId, key.metricTypeId, key.unitId, "WEEK", weekStart, d.sum, d.count);
            repo.addDelta(userId, key.metricTypeId, key.unitId, "MONTH", monthStart, d.sum, d.count);
            repo.addDelta(userId, key.metricTypeId, key.unitId, "HALF_YEAR", halfYearStart, d.sum, d.count);
            repo.addDelta(userId, key.metricTypeId, key.unitId, "YEAR", yearStart, d.sum, d.count);
        }
    }

    // key = (metricTypeId, unitId)
    private static final class Key {
        private final Long metricTypeId;
        private final Long unitId;

        private Key(Long metricTypeId, Long unitId) {
            this.metricTypeId = metricTypeId;
            this.unitId = unitId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return Objects.equals(metricTypeId, key.metricTypeId) &&
                    Objects.equals(unitId, key.unitId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(metricTypeId, unitId);
        }
    }

    private static final class Delta {
        private long sum = 0;
        private int count = 0;
    }

    private void validateRange(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new BadRequestException("dateFrom must be before or equal to dateTo");
        }
    }

    private MetricUsageAggDto toDto(MetricUsageAggRow row) {
        return new MetricUsageAggDto(
                row.getMetricTypeId(),
                row.getMetricTypeLabel(),
                row.getUnitId(),
                row.getUnitLabel(),
                row.getBucket(),
                row.getBucketStart(),
                row.getValueSum(),
                row.getValueCount()
        );
    }
}
