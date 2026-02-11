package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.template.*;
import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateCreateDto;
import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateUpdateDto;
import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateViewDto;
import com.example.activity_diary.dto.template.diary.EntryTemplateMetricUpsertDto;
import com.example.activity_diary.dto.template.diary.EntryTemplateMetricValueUpsertDto;
import com.example.activity_diary.dto.template.diary.EntryTemplateMetricValueViewDto;
import com.example.activity_diary.dto.template.diary.EntryTemplateMetricViewDto;
import com.example.activity_diary.entity.*;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.template.DiaryEntryTemplate;
import com.example.activity_diary.entity.template.EntryTemplateMetric;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.template.DiaryEntryTemplateRepository;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.service.diary.DiaryEntryTemplateService;
import com.example.activity_diary.service.diary.TagResolverService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryEntryTemplateServiceImpl implements DiaryEntryTemplateService {

    private final DiaryEntryTemplateRepository diaryEntryTemplateRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final DictionaryRepository dictionaryRepository;
    private final TagResolverService tagResolverService;

    @Override
    public DiaryEntryTemplateViewDto create(Long userId, DiaryEntryTemplateCreateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String name = normalize(dto.getName());
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Template name is required");
        }
        if (diaryEntryTemplateRepository.existsByUser_IdAndNameIgnoreCase(userId, name)) {
            throw new BadRequestException("Template name already exists");
        }

        validateMood(dto.getMood());

        String desc = dto.getDescription() == null ? null : dto.getDescription().trim();
        if (desc == null || desc.isBlank()) {
            throw new BadRequestException("Description is required");
        }

        Set<Tag> resolvedTags = tagResolverService.resolveFromDescription(userId, desc);
        if (resolvedTags == null || resolvedTags.isEmpty()) {
            throw new BadRequestException("At least one tag is required");
        }

        DiaryEntryTemplate template = DiaryEntryTemplate.create(
                user,
                name,
                dto.getMood(),
                desc
        );

        template.setTags(resolvedTags);

        if (dto.getMetrics() != null) {
            applyMetricsReplace(template, dto.getMetrics());
        }

        DiaryEntryTemplate saved = diaryEntryTemplateRepository.save(template);
        return toViewDto(saved, true);
    }

    @Override
    public DiaryEntryTemplateViewDto update(Long userId, Long templateId, DiaryEntryTemplateUpdateDto dto) {
        DiaryEntryTemplate template = diaryEntryTemplateRepository.findByIdAndUser_Id(templateId, userId)
                .orElseThrow(() -> new NotFoundException("Template not found"));

        if (dto.getName() != null) {
            String name = normalize(dto.getName());
            if (name == null || name.isBlank()) {
                throw new BadRequestException("Template name is required");
            }
            if (!name.equalsIgnoreCase(template.getName())
                    && diaryEntryTemplateRepository.existsByUser_IdAndNameIgnoreCase(userId, name)) {
                throw new BadRequestException("Template name already exists");
            }
            template.updateName(name);
        }

        if (dto.getMood() != null) {
            validateMood(dto.getMood());
            template.updateMood(dto.getMood());
        }

        // description -> пересчитать теги
        if (dto.getDescription() != null) {
            String desc = dto.getDescription().trim();
            if (desc.isBlank()) {
                throw new BadRequestException("Description is required");
            }

            Set<Tag> resolvedTags = tagResolverService.resolveFromDescription(userId, desc);
            if (resolvedTags == null || resolvedTags.isEmpty()) {
                throw new BadRequestException("At least one tag is required");
            }

            template.updateDescription(desc);
            template.setTags(resolvedTags);
        }

        if (dto.getMetrics() != null) {
            applyMetricsReplace(template, dto.getMetrics());
        }

        DiaryEntryTemplate saved = diaryEntryTemplateRepository.save(template);
        return toViewDto(saved, true);
    }

    @Override
    public DiaryEntryTemplateViewDto get(Long userId, Long templateId) {
        DiaryEntryTemplate template = diaryEntryTemplateRepository.findByIdAndUser_Id(templateId, userId)
                .orElseThrow(() -> new NotFoundException("Template not found"));
        return toViewDto(template, true);
    }

    @Override
    public Page<DiaryEntryTemplateViewDto> list(Long userId, Pageable pageable) {
        Page<DiaryEntryTemplate> page = diaryEntryTemplateRepository.findAllByUser_Id(userId, pageable);
        return page.map(t -> toViewDto(t, false));
    }

    @Override
    public void delete(Long userId, Long templateId) {
        DiaryEntryTemplate template = diaryEntryTemplateRepository.findByIdAndUser_Id(templateId, userId)
                .orElseThrow(() -> new NotFoundException("Template not found"));
        diaryEntryTemplateRepository.delete(template);
    }

    // ===================== helpers =====================

    private static String normalize(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v;
    }

    private static void validateMood(Short mood) {
        if (mood != null && (mood < 1 || mood > 5)) {
            throw new BadRequestException("Mood must be between 1 and 5");
        }
    }

    private Set<Tag> resolveTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return Collections.emptySet();

        List<Tag> found = tagRepository.findAllByIdIn(tagIds);
        if (found.size() != tagIds.size()) {
            Set<Long> foundIds = found.stream().map(Tag::getId).collect(Collectors.toSet());
            Set<Long> missing = tagIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toSet());
            throw new BadRequestException("Tags not found: " + missing);
        }
        return new HashSet<>(found);
    }

    private void applyMetricsReplace(DiaryEntryTemplate template, List<EntryTemplateMetricUpsertDto> incoming) {
        template.getMetrics().clear();
        if (incoming == null || incoming.isEmpty()) return;

        Set<Long> dictIds = new HashSet<>();
        for (EntryTemplateMetricUpsertDto m : incoming) {
            dictIds.add(m.getMetricTypeId());
            if (m.getValues() != null) {
                for (EntryTemplateMetricValueUpsertDto v : m.getValues()) {
                    dictIds.add(v.getUnitId());
                }
            }
        }

        Map<Long, DictionaryItem> dictMap = dictionaryRepository.findAllById(dictIds).stream()
                .collect(Collectors.toMap(DictionaryItem::getId, d -> d));

        if (dictMap.size() != dictIds.size()) {
            throw new BadRequestException("Some dictionary items not found");
        }

        for (EntryTemplateMetricUpsertDto m : incoming) {
            DictionaryItem metricType = dictMap.get(m.getMetricTypeId());
            EntryTemplateMetric metric = EntryTemplateMetric.create(template, metricType);

            List<EntryTemplateMetricValueUpsertDto> values =
                    (m.getValues() == null) ? List.of() : m.getValues();

            Set<Long> unitIds = new HashSet<>();
            for (EntryTemplateMetricValueUpsertDto v : values) {
                if (!unitIds.add(v.getUnitId())) {
                    throw new BadRequestException("Duplicate unitId for metricTypeId=" + m.getMetricTypeId());
                }
                DictionaryItem unit = dictMap.get(v.getUnitId());
                metric.addValue(unit, v.getValue());
            }

            template.addMetric(metric);
        }
    }

    private DiaryEntryTemplateViewDto toViewDto(DiaryEntryTemplate t, boolean includeMetrics) {
        Set<TagBriefDto> tags = (t.getTags() == null)
                ? Set.of()
                : t.getTags().stream()
                .map(tag -> new TagBriefDto(tag.getId(), tag.getName()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<EntryTemplateMetricViewDto> metrics = List.of();
        if (includeMetrics) {
            metrics = (t.getMetrics() == null) ? List.of() : t.getMetrics().stream()
                    .map(this::toMetricViewDto)
                    .toList();
        }

        return new DiaryEntryTemplateViewDto(
                t.getId(),
                t.getName(),
                t.getMood(),
                t.getDescription(),
                tags,
                metrics,
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }

    private EntryTemplateMetricViewDto toMetricViewDto(EntryTemplateMetric m) {
        Long typeId = (m.getMetricType() == null) ? null : m.getMetricType().getId();
        String typeName = (m.getMetricType() == null) ? null : m.getMetricType().getLabel();

        List<EntryTemplateMetricValueViewDto> values = (m.getValues() == null)
                ? List.of()
                : m.getValues().stream()
                .map(v -> new EntryTemplateMetricValueViewDto(
                        v.getId(),
                        v.getUnit() == null ? null : v.getUnit().getId(),
                        v.getUnit() == null ? null : v.getUnit().getLabel(),
                        v.getValue() == null ? null : v.getValue().longValue()
                ))
                .toList();

        return new EntryTemplateMetricViewDto(
                m.getId(),
                typeId,
                typeName,
                values
        );
    }
}
