package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.exception.types.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiaryDescriptionTagPolicyTest {

    private final DiaryDescriptionTagPolicy policy = new DiaryDescriptionTagPolicy();

    @Test
    void extractValidTagNames_returnsCanonicalNamesWithoutHash() {
        List<String> tags = List.copyOf(
                policy.extractValidTagNames("Run #Sport #sport #test_1! #a")
        );

        assertEquals(List.of("sport", "test_1!", "a"), tags);
    }

    @Test
    void extractValidTagNames_extractsMultipleCyrillicTags() {
        List<String> tags = List.copyOf(
                policy.extractValidTagNames("сегодня #тренировка на #подтягивания")
        );

        assertEquals(List.of("тренировка", "подтягивания"), tags);
    }

    @Test
    void extractValidTagNames_deduplicatesRepeatedTagsInDescription() {
        List<String> tags = List.copyOf(
                policy.extractValidTagNames("#спорт #спорт")
        );

        assertEquals(List.of("спорт"), tags);
    }

    @Test
    void extractValidTagNames_skipsNamesWithInternalHash() {
        List<String> tags = List.copyOf(
                policy.extractValidTagNames("Run #ta#g #ok")
        );

        assertEquals(List.of("ok"), tags);
    }

    @Test
    void normalizeTagName_removesSingleLeadingHashAndLowercases() {
        assertEquals("sport", policy.normalizeTagName("  #Sport "));
        assertEquals("sport", policy.normalizeTagName(" Sport "));
    }

    @Test
    void isValidTagName_validatesCanonicalName() {
        assertTrue(policy.isValidTagName("tag"));
        assertFalse(policy.isValidTagName("#tag"));
        assertFalse(policy.isValidTagName(""));
        assertFalse(policy.isValidTagName("   "));
        assertFalse(policy.isValidTagName("ta#g"));
    }

    @Test
    void hasAtLeastOneValidTag_returnsFalseWhenNoHashtagExists() {
        assertFalse(policy.hasAtLeastOneValidTag("plain text"));
    }

    @Test
    void hasAtLeastOneValidTag_returnsTrueWhenValidHashtagExists() {
        assertTrue(policy.hasAtLeastOneValidTag("plain text #a"));
    }

    @Test
    void ensureContainsAtLeastOneValidTag_throwsWhenNoValidTagExists() {
        assertThrows(
                BadRequestException.class,
                () -> policy.ensureContainsAtLeastOneValidTag("plain text")
        );
    }
}
