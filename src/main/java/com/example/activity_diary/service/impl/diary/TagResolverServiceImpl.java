package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.*;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.tag.UserTagRepository;
import com.example.activity_diary.service.diary.TagResolverService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagResolverServiceImpl implements TagResolverService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final UserTagRepository userTagRepository;
    private final DiaryDescriptionTagPolicy diaryDescriptionTagPolicy;

    @Transactional
    @Override
    public Set<Tag> resolveFromDescription(Long userId, String description) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (description == null || description.isBlank()) return Collections.emptySet();

        return resolveForUser(
                userId,
                diaryDescriptionTagPolicy.extractValidTagNames(description)
        );
    }

    @Transactional
    @Override
    public Set<Tag> resolveForUser(Long userId, Collection<String> rawNames) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (rawNames == null || rawNames.isEmpty()) return Collections.emptySet();

        User userRef = userRepository.getReferenceById(userId);

        // 1) РЅРѕСЂРјР°Р»РёР·СѓРµРј Рё СѓР±РёСЂР°РµРј РґСѓР±Р»Рё (СЃРѕС…СЂР°РЅСЏРµРј РїРѕСЂСЏРґРѕРє)
        LinkedHashSet<String> names = rawNames.stream()
                .map(diaryDescriptionTagPolicy::normalizeTagName)
                .filter(diaryDescriptionTagPolicy::isValidTagName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (names.isEmpty()) return Collections.emptySet();

        // 2) РѕРґРЅРёРј Р·Р°РїСЂРѕСЃРѕРј Р±РµСЂС‘Рј СЃСѓС‰РµСЃС‚РІСѓСЋС‰РёРµ С‚РµРіРё
        List<Tag> existing = tagRepository.findByNameIn(names);
        Map<String, Tag> byName = existing.stream()
                .collect(Collectors.toMap(Tag::getName, t -> t));

        Set<Tag> result = new HashSet<>();

        for (String name : names) {
            Tag tag = byName.get(name);

            if (tag == null) {
                // 3) СЃРѕР·РґР°С‘Рј PENDING (race-safe РЅР° UNIQUE)
                try {
                    tag = tagRepository.save(Tag.builder()
                            .name(name)
                            .status(TagStatus.PENDING)
                            .createdBy(userRef)
                            .build());
                } catch (DataIntegrityViolationException e) {
                    // РєС‚Рѕ-С‚Рѕ СЃРѕР·РґР°Р» РїР°СЂР°Р»Р»РµР»СЊРЅРѕ
                    tag = tagRepository.findByName(name).orElseThrow(() -> e);
                }
            } else {
                // 4) Р·Р°РїСЂРµС‰Р°РµРј REJECTED
                if (tag.getStatus() == TagStatus.REJECTED) {
                    throw new IllegalArgumentException("Tag is rejected: " + name);
                }

                // 5) РµСЃР»Рё PENDING вЂ” СЃС‡РёС‚Р°РµРј РІ suggestion (РѕРїС†РёРѕРЅР°Р»СЊРЅРѕ)
            }

            result.add(tag);

            // 6) РіР°СЂР°РЅС‚РёСЂСѓРµРј user_tag (РґР»СЏ Р°РІС‚РѕРєРѕРјРїР»РёС‚Р° Рё РЅР°СЃС‚СЂРѕРµРє РїРѕР»СЊР·РѕРІР°С‚РµР»СЏ)
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
}
