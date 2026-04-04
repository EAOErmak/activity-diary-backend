package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.MetricUsageAggDto;
import com.example.activity_diary.dto.analytics.MetricUsageAggFilterDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.TagUsageBucket;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.repository.diary.MetricUsageAggRepository;
import com.example.activity_diary.repository.diary.MetricUsageAggRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricUsageAggServiceImplTest {

    @Mock
    private MetricUsageAggRepository repo;

    @InjectMocks
    private MetricUsageAggServiceImpl service;

    @Test
    void getUsage_returnsRepositoryResult() {
        MetricUsageAggFilterDto filter = new MetricUsageAggFilterDto(
                TagUsageBucket.MONTH,
                21L,
                34L,
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-03-01")
        );
        MetricUsageAggRow row = new MetricUsageAggRow() {
            @Override
            public Long getMetricTypeId() {
                return 21L;
            }

            @Override
            public String getMetricTypeLabel() {
                return "distance";
            }

            @Override
            public Long getUnitId() {
                return 34L;
            }

            @Override
            public String getUnitLabel() {
                return "km";
            }

            @Override
            public TagUsageBucket getBucket() {
                return TagUsageBucket.MONTH;
            }

            @Override
            public LocalDate getBucketStart() {
                return LocalDate.parse("2026-02-01");
            }

            @Override
            public long getValueSum() {
                return 120;
            }

            @Override
            public int getValueCount() {
                return 4;
            }
        };

        when(repo.findUsageRows(
                7L,
                filter.getBucket(),
                filter.getMetricTypeId(),
                filter.getUnitId(),
                filter.getDateFrom(),
                filter.getDateTo()
        )).thenReturn(List.of(row));

        List<MetricUsageAggDto> actual = service.getUsage(7L, filter);

        assertEquals(1, actual.size());
        assertEquals(21L, actual.getFirst().getMetricTypeId());
        assertEquals("distance", actual.getFirst().getMetricTypeLabel());
        assertEquals(34L, actual.getFirst().getUnitId());
        assertEquals("km", actual.getFirst().getUnitLabel());
        assertEquals(TagUsageBucket.MONTH, actual.getFirst().getBucket());
        assertEquals(LocalDate.parse("2026-02-01"), actual.getFirst().getBucketStart());
        assertEquals(120, actual.getFirst().getValueSum());
        assertEquals(4, actual.getFirst().getValueCount());
        verify(repo).findUsageRows(
                7L,
                filter.getBucket(),
                filter.getMetricTypeId(),
                filter.getUnitId(),
                filter.getDateFrom(),
                filter.getDateTo()
        );
    }

    @Test
    void getUsage_whenDateRangeInvalid_throwsBadRequest() {
        MetricUsageAggFilterDto filter = new MetricUsageAggFilterDto(
                TagUsageBucket.WEEK,
                null,
                null,
                LocalDate.parse("2026-03-10"),
                LocalDate.parse("2026-03-01")
        );

        assertThrows(BadRequestException.class, () -> service.getUsage(1L, filter));
    }

    @Test
    void onEntryDeleted_appliesNegativeDelta() {
        DiaryEntry entry = DiaryEntry.builder()
                .user(userWithId(7L))
                .whenStarted(Instant.parse("2026-03-18T08:00:00Z"))
                .build();

        DictionaryItem metricType = dictionaryItem(21L, DictionaryType.METRIC_NAME);
        DictionaryItem unit = dictionaryItem(34L, DictionaryType.METRIC_UNIT);
        EntryMetric metric = EntryMetric.create(entry, metricType);
        metric.addValue(unit, 12);
        entry.addMetric(metric);

        service.onEntryDeleted(entry);

        verify(repo).addDelta(7L, 21L, 34L, "DAY", LocalDate.parse("2026-03-18"), -12L, -1);
        verify(repo).addDelta(7L, 21L, 34L, "WEEK", LocalDate.parse("2026-03-16"), -12L, -1);
        verify(repo).addDelta(7L, 21L, 34L, "MONTH", LocalDate.parse("2026-03-01"), -12L, -1);
        verify(repo).addDelta(7L, 21L, 34L, "HALF_YEAR", LocalDate.parse("2026-01-01"), -12L, -1);
        verify(repo).addDelta(7L, 21L, 34L, "YEAR", LocalDate.parse("2026-01-01"), -12L, -1);
    }

    private static User userWithId(Long id) {
        User user = User.builder().username("user").build();
        user.setId(id);
        return user;
    }

    private static DictionaryItem dictionaryItem(Long id, DictionaryType type) {
        DictionaryItem item = DictionaryItem.builder()
                .type(type)
                .label("item-" + id)
                .build();
        item.setId(id);
        return item;
    }
}
