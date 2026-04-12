package com.example.activity_diary.service.impl.diary;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiaryEntryTagFilterNormalizerTest {

    @Test
    void normalize_nullTags_returnsEmptyResult() {
        var result = DiaryEntryTagFilterNormalizer.normalize(null);

        assertEquals(List.of(), result.tagNames());
        assertFalse(result.hasTags());
        assertEquals(0, result.tagCount());
    }

    @Test
    void normalize_blankAndDuplicateTags_filtersAndNormalizesValues() {
        var result = DiaryEntryTagFilterNormalizer.normalize(
                java.util.Arrays.asList("  #Foo! ", "", "#BAR", "#foo!", null)
        );

        assertEquals(List.of("#foo!", "#bar"), result.tagNames());
        assertTrue(result.hasTags());
        assertEquals(2, result.tagCount());
    }

    @Test
    void normalize_onlyBlankTags_returnsEmptyResult() {
        var result = DiaryEntryTagFilterNormalizer.normalize(
                List.of(" ", "   ", "")
        );

        assertEquals(List.of(), result.tagNames());
        assertFalse(result.hasTags());
        assertEquals(0, result.tagCount());
    }
}
