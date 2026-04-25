package com.example.activity_diary.service.impl.diary;

import java.util.List;

public final class DiaryEntryTagFilterNormalizer {

    private DiaryEntryTagFilterNormalizer() {
    }

    public static NormalizedTags normalize(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new NormalizedTags(List.of(), false, 0);
        }

        List<String> tagNames = tags.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(DiaryDescriptionTagPolicy::normalizeCanonicalTagName)
                .filter(DiaryDescriptionTagPolicy::isValidCanonicalTagName)
                .distinct()
                .toList();

        return new NormalizedTags(tagNames, !tagNames.isEmpty(), tagNames.size());
    }

    public record NormalizedTags(List<String> tagNames, boolean hasTags, int tagCount) {
    }
}
