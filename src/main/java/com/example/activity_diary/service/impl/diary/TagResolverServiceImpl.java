package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.*;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.repository.tag.TagSuggestionRepository;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.tag.UserTagRepository;
import com.example.activity_diary.service.diary.TagResolverService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagResolverServiceImpl implements TagResolverService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final UserTagRepository userTagRepository;
    private final TagSuggestionRepository tagSuggestionRepository;

    /**
     * Правило:
     * - тег начинается с '#'
     * - внутри только буквы + '_' + '-'
     * - тег заканчивается пробелом/переводом строки/концом текста/знаком пунктуации
     * - '#' должен быть в начале строки или после пробела (не "тест#спорт")
     */
    private static final Pattern TAG_PATTERN =
            Pattern.compile("(?<!\\S)#([\\p{L}\\p{N}_-]+)(?=\\s|$|\\p{P})");

    @Transactional
    @Override
    public Set<Tag> resolveFromDescription(Long userId, String description) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (description == null || description.isBlank()) return Collections.emptySet();

        LinkedHashSet<String> rawNames = new LinkedHashSet<>();
        Matcher m = TAG_PATTERN.matcher(description);

        while (m.find()) {
            String raw = m.group(1);
            rawNames.add(raw);
        }

        return resolveForUser(userId, rawNames);
    }

    /**
     * Делает Set<Tag> из имён:
     * - нормализует
     * - создаёт PENDING Tag если нет
     * - запрещает REJECTED
     * - гарантирует UserTag(user, tag)
     * - ведёт TagSuggestion (упрощённо: счётчик может быть завышен без таблицы уникальности)
     */
    @Transactional
    @Override
    public Set<Tag> resolveForUser(Long userId, Collection<String> rawNames) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (rawNames == null || rawNames.isEmpty()) return Collections.emptySet();

        User userRef = userRepository.getReferenceById(userId);

        // 1) нормализуем и убираем дубли (сохраняем порядок)
        LinkedHashSet<String> names = rawNames.stream()
                .map(this::normalize)
                .filter(s -> s != null && !s.isBlank())
                .filter(this::isValidTagName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (names.isEmpty()) return Collections.emptySet();

        // 2) одним запросом берём существующие теги
        List<Tag> existing = tagRepository.findByNameIn(names);
        Map<String, Tag> byName = existing.stream()
                .collect(Collectors.toMap(Tag::getName, t -> t));

        Set<Tag> result = new HashSet<>();

        for (String name : names) {
            Tag tag = byName.get(name);

            if (tag == null) {
                // 3) создаём PENDING (race-safe на UNIQUE)
                try {
                    tag = tagRepository.save(Tag.builder()
                            .name(name)
                            .status(TagStatus.PENDING)
                            .createdBy(userRef)
                            .build());
                    upsertSuggestion(name);
                } catch (DataIntegrityViolationException e) {
                    // кто-то создал параллельно
                    tag = tagRepository.findByName(name).orElseThrow(() -> e);
                }
            } else {
                // 4) запрещаем REJECTED
                if (tag.getStatus() == TagStatus.REJECTED) {
                    throw new IllegalArgumentException("Tag is rejected: " + name);
                }

                // 5) если PENDING — считаем в suggestion (опционально)
                if (tag.getStatus() == TagStatus.PENDING) {
                    upsertSuggestion(name);
                }
            }

            result.add(tag);

            // 6) гарантируем user_tag (для автокомплита и настроек пользователя)
            UserTagId id = new UserTagId(userId, tag.getId());
            if (!userTagRepository.existsById(id)) {
                userTagRepository.save(UserTag.builder()
                        .id(id)
                        .user(userRef)
                        .tag(tag)
                        .build());
            }
        }

        return result;
    }

    private void upsertSuggestion(String normalizedName) {
        Instant now = Instant.now();

        TagSuggestion s = tagSuggestionRepository.findByTagName(normalizedName)
                .orElseGet(() -> TagSuggestion.builder()
                        .tagName(normalizedName)
                        .userCount(0)
                        .lastSeenAt(now)
                        .build());

        // ВНИМАНИЕ: это не "уникальные пользователи", а "количество использований"
        // Для уникальности нужна доп. таблица tag_suggestion_user(tag_name, user_id).
        s.incrementUserCount();
        s.markSeen(now);

        tagSuggestionRepository.save(s);
    }

    private String normalize(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase(Locale.ROOT);
        s = s.replaceAll("[^\\p{L}\\p{N}_\\-]+", "");
        return s;
    }

    private boolean isValidTagName(String s) {
        return s.length() >= 2 && s.length() <= 32;
    }
}
