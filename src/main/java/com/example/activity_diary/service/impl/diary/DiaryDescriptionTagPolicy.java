package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.exception.types.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DiaryDescriptionTagPolicy {

    /**
     * Правило:
     * - тег начинается с '#'
     * - внутри только буквы + цифры + '_' + '-'
     * - тег заканчивается пробелом/концом текста/знаком пунктуации
     * - '#' должен быть в начале строки или после пробела
     */
    private static final Pattern TAG_PATTERN =
            Pattern.compile("(?<!\\S)#([\\p{L}\\p{N}_-]+)(?=\\s|$|\\p{P})");

    public LinkedHashSet<String> extractValidTagNames(String description) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        if (description == null || description.isBlank()) {
            return names;
        }

        Matcher matcher = TAG_PATTERN.matcher(description);
        while (matcher.find()) {
            String normalized = normalize(matcher.group(1));
            if (normalized != null && !normalized.isBlank() && isValidTagName(normalized)) {
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

    private String normalize(String raw) {
        if (raw == null) return null;
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return normalized.replaceAll("[^\\p{L}\\p{N}_\\-]+", "");
    }

    private boolean isValidTagName(String value) {
        return value.length() >= 2 && value.length() <= 32;
    }
}
