package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.MetricUsageAggDto;
import com.example.activity_diary.dto.analytics.MetricUsageAggFilterDto;
import com.example.activity_diary.entity.enums.TagUsageBucket;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.repository.diary.MetricUsageAggRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
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
        List<MetricUsageAggDto> expected = List.of(
                new MetricUsageAggDto(
                        21L,
                        "distance",
                        34L,
                        "km",
                        TagUsageBucket.MONTH,
                        LocalDate.parse("2026-02-01"),
                        120,
                        4
                )
        );

        when(repo.findUsage(
                7L,
                filter.getBucket(),
                filter.getMetricTypeId(),
                filter.getUnitId(),
                filter.getDateFrom(),
                filter.getDateTo()
        )).thenReturn(expected);

        List<MetricUsageAggDto> actual = service.getUsage(7L, filter);

        assertSame(expected, actual);
        verify(repo).findUsage(
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
}
