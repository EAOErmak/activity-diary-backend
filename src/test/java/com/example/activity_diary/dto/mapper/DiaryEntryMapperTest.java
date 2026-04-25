package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.enums.TagStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiaryEntryMapperTest {

    private final DiaryEntryMapper mapper = Mappers.getMapper(DiaryEntryMapper.class);

    @Test
    void toDto_sortsMetricsAndValuesByDictionaryItemId() {
        DiaryEntry entry = entry();

        EntryMetric secondMetric = EntryMetric.create(entry, dictItem(20L, DictionaryType.METRIC_NAME, "m20"));
        secondMetric.addValue(dictItem(200L, DictionaryType.METRIC_UNIT, "u200"), BigDecimal.valueOf(5));
        secondMetric.addValue(dictItem(100L, DictionaryType.METRIC_UNIT, "u100"), BigDecimal.valueOf(3));
        entry.addMetric(secondMetric);

        EntryMetric firstMetric = EntryMetric.create(entry, dictItem(10L, DictionaryType.METRIC_NAME, "m10"));
        firstMetric.addValue(dictItem(300L, DictionaryType.METRIC_UNIT, "u300"), BigDecimal.valueOf(7));
        entry.addMetric(firstMetric);

        DiaryEntryDto result = mapper.toDto(entry);

        assertEquals(
                List.of(10L, 20L),
                result.getMetrics().stream()
                        .map(metric -> metric.getMetricTypeId())
                        .toList()
        );
        assertEquals(
                List.of(100L, 200L),
                result.getMetrics().get(1).getValues().stream()
                        .map(value -> value.getUnitId())
                        .toList()
        );
    }

    @Test
    void toListDto_returnsCanonicalFirstTagWithoutHash() {
        DiaryEntry entry = entry();
        Tag first = Tag.builder()
                .name("sport")
                .status(TagStatus.APPROVED)
                .build();
        first.setId(1L);
        Tag second = Tag.builder()
                .name("health")
                .status(TagStatus.APPROVED)
                .build();
        second.setId(2L);
        entry.setTags(new LinkedHashSet<>(List.of(second, first)));

        assertEquals("sport", mapper.toListDto(entry).getFirstTag());
    }

    private static DiaryEntry entry() {
        User user = User.builder().username("user").build();
        user.setId(1L);

        DiaryEntry entry = DiaryEntry.builder()
                .user(user)
                .whenStarted(Instant.parse("2026-02-10T10:00:00Z"))
                .whenEnded(Instant.parse("2026-02-10T10:10:00Z"))
                .duration(10)
                .mood((short) 3)
                .description("#desc")
                .status(EntryStatus.FINISHED)
                .build();
        entry.setId(1L);
        return entry;
    }

    private static DictionaryItem dictItem(Long id, DictionaryType type, String label) {
        DictionaryItem item = DictionaryItem.builder()
                .type(type)
                .label(label)
                .build();
        item.setId(id);
        return item;
    }
}
