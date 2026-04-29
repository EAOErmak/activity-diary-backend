package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateCreateDto;
import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateUpdateDto;
import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateViewDto;
import com.example.activity_diary.dto.template.diary.EntryTemplateMetricUpsertDto;
import com.example.activity_diary.dto.template.diary.EntryTemplateMetricValueUpsertDto;
import com.example.activity_diary.dto.template.diary.EntryTemplateMetricValueViewDto;
import com.example.activity_diary.dto.template.diary.EntryTemplateMetricViewDto;
import com.example.activity_diary.entity.*;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.template.DiaryEntryTemplate;
import com.example.activity_diary.entity.template.EntryTemplateMetric;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.template.DiaryEntryTemplateRepository;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.service.diary.DiaryEntryTemplateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryEntryTemplateServiceImpl implements DiaryEntryTemplateService {

    private final DiaryEntryTemplateRepository diaryEntryTemplateRepository;
    private final UserRepository userRepository;
    private final DictionaryRepository dictionaryRepository;
    private final DiaryDescriptionTagPolicy diaryDescriptionTagPolicy;
    private final EntryTemplateMetricDetailsLoader entryTemplateMetricDetailsLoader;

    @Override
    public DiaryEntryTemplateViewDto create(Long userId, DiaryEntryTemplateCreateDto dto) {
        User user = userRepository.getReferenceById(userId);

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
        diaryDescriptionTagPolicy.ensureContainsAtLeastOneValidTag(desc);

        validateTime(dto.getTimeStart(), dto.getTimeEnd());

        DiaryEntryTemplate template = DiaryEntryTemplate.create(
                user,
                name,
                dto.getMood(),
                desc,
                dto.getTimeStart(),
                dto.getTimeEnd()
        );

        if (dto.getMetrics() != null) {
            applyMetricsReplace(template, dto.getMetrics());
        }

        DiaryEntryTemplate saved = diaryEntryTemplateRepository.save(template);
        return toViewDto(saved, saved.getMetrics(), true);
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

        if (dto.getDescription() != null) {
            String desc = dto.getDescription().trim();
            if (desc.isBlank()) {
                throw new BadRequestException("Description is required");
            }
            diaryDescriptionTagPolicy.ensureContainsAtLeastOneValidTag(desc);
            template.updateDescription(desc);
        }

        if (dto.getTimeStart() != null || dto.getTimeEnd() != null) {
            LocalTime start = (dto.getTimeStart() != null) ? dto.getTimeStart() : template.getTimeStart();
            LocalTime end   = (dto.getTimeEnd() != null) ? dto.getTimeEnd() : template.getTimeEnd();

            validateTime(start, end);
            template.updateTime(start, end);
        }

        if (dto.getMetrics() != null) {
            applyMetricsReplace(template, dto.getMetrics());
        }

        DiaryEntryTemplate saved = diaryEntryTemplateRepository.save(template);
        List<EntryTemplateMetric> metrics = dto.getMetrics() != null
                ? saved.getMetrics()
                : entryTemplateMetricDetailsLoader.loadForTemplate(saved.getId());
        return toViewDto(saved, metrics, true);
    }

    @Override
    public DiaryEntryTemplateViewDto get(Long userId, Long templateId) {
        DiaryEntryTemplate template = diaryEntryTemplateRepository.findByIdAndUser_Id(templateId, userId)
                .orElseThrow(() -> new NotFoundException("Template not found"));
        return toViewDto(template, entryTemplateMetricDetailsLoader.loadForTemplate(templateId), true);
    }

    @Override
    public Page<DiaryEntryTemplateViewDto> list(Long userId, Pageable pageable) {
        Page<DiaryEntryTemplate> page = diaryEntryTemplateRepository.findAllByUser_Id(userId, pageable);
        return page.map(t -> toViewDto(t, List.of(), false));
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

        List<ResolvedTemplateMetric> resolvedMetrics = new ArrayList<>();

        for (EntryTemplateMetricUpsertDto m : incoming) {
            DictionaryItem metricType = resolveDictionary(dictMap, m.getMetricTypeId(), DictionaryType.METRIC_NAME);
            List<EntryTemplateMetricValueUpsertDto> values =
                    (m.getValues() == null) ? List.of() : m.getValues();

            Set<Long> unitIds = new HashSet<>();
            List<ResolvedTemplateMetricValue> resolvedValues = new ArrayList<>();
            for (EntryTemplateMetricValueUpsertDto v : values) {
                if (!unitIds.add(v.getUnitId())) {
                    throw new BadRequestException("Duplicate unitId for metricTypeId=" + m.getMetricTypeId());
                }
                DictionaryItem unit = resolveDictionary(dictMap, v.getUnitId(), DictionaryType.METRIC_UNIT);
                resolvedValues.add(new ResolvedTemplateMetricValue(unit, v.getValue()));
            }

            resolvedValues.sort(Comparator.comparing(value -> value.unit().getId()));
            resolvedMetrics.add(new ResolvedTemplateMetric(metricType, resolvedValues));
        }

        resolvedMetrics.sort(Comparator.comparing(metric -> metric.metricType().getId()));

        for (ResolvedTemplateMetric resolvedMetric : resolvedMetrics) {
            EntryTemplateMetric metric = EntryTemplateMetric.create(template, resolvedMetric.metricType());

            for (ResolvedTemplateMetricValue value : resolvedMetric.values()) {
                metric.addValue(value.unit(), value.value());
            }

            template.addMetric(metric);
        }
    }

    private DictionaryItem resolveDictionary(
            Map<Long, DictionaryItem> dictMap,
            Long id,
            DictionaryType type
    ) {
        DictionaryItem item = dictMap.get(id);
        if (item == null) {
            throw new BadRequestException("Some dictionary items not found");
        }
        if (item.getType() != type) {
            throw new BadRequestException("Invalid dictionary type");
        }
        return item;
    }

    private DiaryEntryTemplateViewDto toViewDto(
            DiaryEntryTemplate t,
            List<EntryTemplateMetric> metricDetails,
            boolean includeMetrics
    ) {
        if (t == null) return null;
        List<EntryTemplateMetricViewDto> metrics = List.of();
        if (includeMetrics) {
            List<EntryTemplateMetric> sourceMetrics = metricDetails == null ? List.of() : metricDetails;
            metrics = sourceMetrics.stream()
                    .sorted(Comparator.comparing(metric -> metric.getMetricType().getId()))
                    .map(this::toMetricViewDto)
                    .toList();
        }

        return new DiaryEntryTemplateViewDto(
                t.getId(),
                t.getName(),
                t.getMood(),
                t.getDescription(),
                t.getTimeStart(),
                t.getTimeEnd(),
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
                .sorted(Comparator.comparing(value -> value.getUnit().getId()))
                .map(v -> new EntryTemplateMetricValueViewDto(
                        v.getId(),
                        v.getUnit() == null ? null : v.getUnit().getId(),
                        v.getUnit() == null ? null : v.getUnit().getLabel(),
                        v.getValue()
                ))
                .toList();

        return new EntryTemplateMetricViewDto(
                m.getId(),
                typeId,
                typeName,
                values
        );
    }

    private static void validateTime(java.time.LocalTime start, java.time.LocalTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BadRequestException("End time cannot be before start time");
        }
    }

    private record ResolvedTemplateMetric(
            DictionaryItem metricType,
            List<ResolvedTemplateMetricValue> values
    ) {
    }

    private record ResolvedTemplateMetricValue(DictionaryItem unit, java.math.BigDecimal value) {
    }
}
