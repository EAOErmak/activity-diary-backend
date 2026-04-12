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
    void extractValidTagNames_preservesCharactersUntilFirstWhitespace() {
        List<String> tags = List.copyOf(
                policy.extractValidTagNames("Run #Sport #sport #test_1! #a")
        );

        assertEquals(List.of("#sport", "#test_1!", "#a"), tags);
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
