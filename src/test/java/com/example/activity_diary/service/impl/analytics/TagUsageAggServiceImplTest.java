package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.TagUsageAggDto;
import com.example.activity_diary.dto.analytics.TagUsageAggFilterDto;
import com.example.activity_diary.entity.enums.TagUsageBucket;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.repository.tag.TagUsageAggRepository;
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
class TagUsageAggServiceImplTest {

    @Mock
    private TagUsageAggRepository repo;

    @InjectMocks
    private TagUsageAggServiceImpl service;

    @Test
    void getUsage_returnsRepositoryResult() {
        TagUsageAggFilterDto filter = new TagUsageAggFilterDto(
                TagUsageBucket.MONTH,
                11L,
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-03-01")
        );
        List<TagUsageAggDto> expected = List.of(
                new TagUsageAggDto(11L, "sport", TagUsageBucket.MONTH, LocalDate.parse("2026-02-01"), 3, 90)
        );

        when(repo.findUsage(7L, filter.getBucket(), filter.getTagId(), filter.getDateFrom(), filter.getDateTo()))
                .thenReturn(expected);

        List<TagUsageAggDto> actual = service.getUsage(7L, filter);

        assertSame(expected, actual);
        verify(repo).findUsage(7L, filter.getBucket(), filter.getTagId(), filter.getDateFrom(), filter.getDateTo());
    }

    @Test
    void getUsage_whenDateRangeInvalid_throwsBadRequest() {
        TagUsageAggFilterDto filter = new TagUsageAggFilterDto(
                TagUsageBucket.WEEK,
                null,
                LocalDate.parse("2026-03-10"),
                LocalDate.parse("2026-03-01")
        );

        assertThrows(BadRequestException.class, () -> service.getUsage(1L, filter));
    }
}
