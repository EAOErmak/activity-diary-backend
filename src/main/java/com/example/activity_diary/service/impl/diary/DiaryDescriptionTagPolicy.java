package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.exception.types.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DiaryDescriptionTagPolicy {

    private static final Pattern TAG_PATTERN = Pattern.compile("#\\S+");
    private static final int MAX_TAG_NAME_LENGTH = 64;

    public LinkedHashSet<String> extractValidTagNames(String description) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        if (description == null || description.isBlank()) {
            return names;
        }

        Matcher matcher = TAG_PATTERN.matcher(description);
        while (matcher.find()) {
            String normalized = normalizeTagName(matcher.group());
            if (isValidTagName(normalized)) {
                names.add(normalized);
            }
        }

        return names;
    }

    public boolean hasAtLeastOneValidTag(String description) {
        return !extractValidTagNames(description).isEmpty();
    }

    public void ensureContainsAtLeastOneValidTag(String description) {
        if (!hasAtLeastOneValidTag(description)) {
            throw new BadRequestException("At least one tag is required");
        }
    }

    public String normalizeTagName(String raw) {
        if (raw == null) return null;

        String value = raw.trim().toLowerCase(Locale.ROOT);

        while (value.startsWith("#")) {
            value = value.substring(1);
        }

        return value.trim();
    }

    public boolean isValidTagName(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        return value.length() <= MAX_TAG_NAME_LENGTH
                && value.chars().noneMatch(Character::isWhitespace);
    }
}
