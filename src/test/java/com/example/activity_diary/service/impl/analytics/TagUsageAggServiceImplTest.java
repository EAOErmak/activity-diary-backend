package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.TagUsageAggDto;
import com.example.activity_diary.dto.analytics.TagUsageAggFilterDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.entity.enums.TagUsageBucket;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.repository.tag.TagUsageAggRepository;
import com.example.activity_diary.repository.tag.TagUsageAggRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Set;

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
        List<TagUsageAggRow> rows = List.of(
                row(11L, "sport", TagUsageBucket.MONTH, LocalDate.parse("2026-02-01"), 3, 90L)
        );

        when(repo.findUsageRows(7L, filter.getBucket(), filter.getTagId(), filter.getDateFrom(), filter.getDateTo()))
                .thenReturn(rows);

        List<TagUsageAggDto> actual = service.getUsage(7L, filter);

        org.junit.jupiter.api.Assertions.assertEquals(expected.size(), actual.size());
        org.junit.jupiter.api.Assertions.assertEquals(expected.getFirst().getTagId(), actual.getFirst().getTagId());
        org.junit.jupiter.api.Assertions.assertEquals(expected.getFirst().getTagName(), actual.getFirst().getTagName());
        org.junit.jupiter.api.Assertions.assertEquals(expected.getFirst().getBucket(), actual.getFirst().getBucket());
        org.junit.jupiter.api.Assertions.assertEquals(expected.getFirst().getBucketStart(), actual.getFirst().getBucketStart());
        org.junit.jupiter.api.Assertions.assertEquals(expected.getFirst().getUsageCount(), actual.getFirst().getUsageCount());
        org.junit.jupiter.api.Assertions.assertEquals(expected.getFirst().getTotalDurationMinutes(), actual.getFirst().getTotalDurationMinutes());
        verify(repo).findUsageRows(7L, filter.getBucket(), filter.getTagId(), filter.getDateFrom(), filter.getDateTo());
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

    @Test
    void onEntryDeleted_appliesNegativeDelta() {
        DiaryEntry entry = DiaryEntry.builder()
                .user(userWithId(7L))
                .whenStarted(Instant.parse("2026-03-18T08:00:00Z"))
                .duration(45)
                .tags(Set.of(tagWithId(11L)))
                .build();

        service.onEntryDeleted(entry);

        verify(repo).addDelta(7L, 11L, "DAY", LocalDate.parse("2026-03-18"), -1, -45L);
        verify(repo).addDelta(7L, 11L, "WEEK", LocalDate.parse("2026-03-16"), -1, -45L);
        verify(repo).addDelta(7L, 11L, "MONTH", LocalDate.parse("2026-03-01"), -1, -45L);
        verify(repo).addDelta(7L, 11L, "HALF_YEAR", LocalDate.parse("2026-01-01"), -1, -45L);
        verify(repo).addDelta(7L, 11L, "YEAR", LocalDate.parse("2026-01-01"), -1, -45L);
    }

    private static User userWithId(Long id) {
        User user = User.builder().username("user").build();
        user.setId(id);
        return user;
    }

    private static Tag tagWithId(Long id) {
        Tag tag = Tag.builder()
                .name("sport")
                .status(TagStatus.APPROVED)
                .build();
        tag.setId(id);
        return tag;
    }

    private static TagUsageAggRow row(
            Long tagId,
            String tagName,
            TagUsageBucket bucket,
            LocalDate bucketStart,
            int usageCount,
            long totalDurationMinutes
    ) {
        return new TagUsageAggRow() {
            @Override
            public Long getTagId() {
                return tagId;
            }

            @Override
            public String getTagName() {
                return tagName;
            }

            @Override
            public TagUsageBucket getBucket() {
                return bucket;
            }

            @Override
            public LocalDate getBucketStart() {
                return bucketStart;
            }

            @Override
            public int getUsageCount() {
                return usageCount;
            }

            @Override
            public long getTotalDurationMinutes() {
                return totalDurationMinutes;
            }
        };
    }
}
