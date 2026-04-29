package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.diary.DiaryEntryViewDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricCreateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricUpdateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueCreateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueUpdateDto;
import com.example.activity_diary.dto.mapper.DiaryEntryMapper;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DiaryEntryCreateMode;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.service.analytics.MetricUsageAggService;
import com.example.activity_diary.service.analytics.TagUsageAggService;
import com.example.activity_diary.service.diary.DiaryService;
import com.example.activity_diary.service.diary.DiaryValidationService;
import com.example.activity_diary.service.diary.TagMetricService;
import com.example.activity_diary.service.diary.TagResolverService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryServiceImpl implements DiaryService {

    private static final ZoneId RANGE_QUERY_ZONE = ZoneId.systemDefault();

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final DictionaryRepository dictionaryRepository;

    private final DiaryValidationService validationService;
    private final TagResolverService tagResolverService;
    private final DiaryDescriptionTagPolicy diaryDescriptionTagPolicy;
    private final DiaryEntryMapper mapper;
    private final TagUsageAggService tagUsageAggService;
    private final MetricUsageAggService metricUsageAggService;
    private final TagMetricService tagMetricService;
    private final EntryMetricDetailsLoader entryMetricDetailsLoader;

    @Override
    @Transactional(readOnly = true)
    public List<DiaryEntryViewDto> getAllEntries(Long userId) {
        return diaryRepository.findAllByUserId(userId);
    }

    @Override
    public Slice<DiaryEntryViewDto> getMyEntries(Long userId, Pageable pageable) {
        return diaryRepository.findListByUserId(userId, pageable);
    }

    @Override
    public Slice<DiaryEntryViewDto> getMyEntriesFiltered(
            Long userId,
            EntryStatus status,
            List<String> tags,
            Instant from,
            Instant to,
            Pageable pageable
    ) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("from must be <= to");
        }

        var normalizedTags = DiaryEntryTagFilterNormalizer.normalize(tags);

        if (normalizedTags.hasTags()) {
            return diaryRepository.findListByUserIdFilteredAndTags(
                    userId,
                    status,
                    normalizedTags.tagNames(),
                    normalizedTags.tagCount(),
                    from,
                    to,
                    pageable
            );
        }

        return diaryRepository.findListByUserIdFiltered(
                userId,
                status,
                from,
                to,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiaryEntryViewDto> getEntriesByDateRange(
            Long userId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new BadRequestException("Invalid date range");
        }

        Instant fromInstant = from.atZone(RANGE_QUERY_ZONE).toInstant();
        Instant toInstant = to.atZone(RANGE_QUERY_ZONE).toInstant();

        return diaryRepository.findByUserAndDateRange(userId, fromInstant, toInstant).stream()
                .map(mapper::toListDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DiaryEntryDto getMyEntryById(Long id, Long userId) {
        DiaryEntry entry = diaryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Entry not found"));

        return mapper.toDetailedDto(entry, entryMetricDetailsLoader.loadForEntry(id));
    }

    @Override
    public DiaryEntryDto create(DiaryEntryCreateDto dto, Long userId, DiaryEntryCreateMode mode) {
        validationService.validateCreate(dto);

        var user = userRepository.getReferenceById(userId);

        String description = dto.getDescription() == null ? null : dto.getDescription().trim();
        if (description == null || description.isBlank()) {
            throw new BadRequestException("Description is required");
        }

        DiaryEntry entry = DiaryEntry.create(
                user,
                dto.getWhenStarted(),
                dto.getWhenEnded(),
                dto.getMood(),
                description
        );

        Set<Tag> tags = resolveRequiredTags(userId, description);

        if (mode == DiaryEntryCreateMode.CONFIRM_GOAL) {
            entry.forceStatusWin();
        }

        applyMetricsOnCreate(dto.getMetrics(), entry, tags);
        entry.setTags(tags);

        DiaryEntry saved = diaryRepository.save(entry);
        metricUsageAggService.onEntryCreated(saved);
        tagUsageAggService.onEntryCreated(saved);
        return mapper.toDto(saved);
    }

    @Override
    public List<DiaryEntryDto> createAll(List<DiaryEntryCreateDto> dtos, Long userId, DiaryEntryCreateMode mode) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }

        List<PreparedCreate> preparedCreates = prepareCreates(dtos, userId);
        var user = userRepository.getReferenceById(userId);

        Set<Long> dictionaryIds = new HashSet<>();
        for (PreparedCreate preparedCreate : preparedCreates) {
            dictionaryIds.addAll(collectDictionaryIdsForCreate(preparedCreate.dto().getMetrics()));
        }
        Map<Long, DictionaryItem> dictionaryItems = loadDictionaryItems(dictionaryIds);

        List<DiaryEntry> entriesToSave = new ArrayList<>(preparedCreates.size());
        Set<MetricValidationKey> validatedMetricSets = new HashSet<>();
        for (PreparedCreate preparedCreate : preparedCreates) {
            DiaryEntry entry = DiaryEntry.create(
                    user,
                    preparedCreate.dto().getWhenStarted(),
                    preparedCreate.dto().getWhenEnded(),
                    preparedCreate.dto().getMood(),
                    preparedCreate.description()
            );

            if (mode == DiaryEntryCreateMode.CONFIRM_GOAL) {
                entry.forceStatusWin();
            }

            List<ResolvedMetric> resolvedMetrics = resolveMetricsForCreate(
                    preparedCreate.dto().getMetrics(),
                    dictionaryItems
            );
            List<Long> metricTypeIds = metricTypeIds(resolvedMetrics);
            MetricValidationKey validationKey = new MetricValidationKey(
                    tagIds(preparedCreate.tags()),
                    metricTypeIds
            );
            if (validatedMetricSets.add(validationKey)) {
                tagMetricService.validateMetricTypesAllowedForTags(
                        preparedCreate.tags(),
                        metricTypeIds
                );
            }
            applyResolvedMetrics(entry, resolvedMetrics);
            entry.setTags(preparedCreate.tags());
            entriesToSave.add(entry);
        }

        List<DiaryEntry> savedEntries = diaryRepository.saveAll(entriesToSave);
        for (DiaryEntry savedEntry : savedEntries) {
            metricUsageAggService.onEntryCreated(savedEntry);
            tagUsageAggService.onEntryCreated(savedEntry);
        }

        return savedEntries.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public DiaryEntryDto update(Long id, DiaryEntryUpdateDto dto, Long userId) {
        validationService.validateUpdate(dto);

        DiaryEntry entry = getEntryGraphForUser(id, userId);
        entryMetricDetailsLoader.loadForEntry(id);

        Set<Tag> targetTags = entry.getTags();
        String desc = null;
        if (dto.getDescription() != null) {
            desc = dto.getDescription().trim();
            targetTags = resolveRequiredTags(userId, desc);
        }

        List<ResolvedMetric> resolvedMetrics = null;
        if (dto.getMetrics() != null) {
            resolvedMetrics = resolveMetricsForUpdate(dto.getMetrics());
            tagMetricService.validateMetricTypesAllowedForTags(targetTags, metricTypeIds(resolvedMetrics));
        } else if (dto.getDescription() != null && !entry.getMetrics().isEmpty()) {
            tagMetricService.validateMetricTypesAllowedForTags(targetTags, currentMetricTypeIds(entry));
        }

        metricUsageAggService.onEntryDeleted(entry);
        tagUsageAggService.onEntryDeleted(entry);

        if (dto.getWhenStarted() != null && dto.getWhenEnded() != null) {
            entry.updateTime(dto.getWhenStarted(), dto.getWhenEnded());
        }

        if (dto.getDescription() != null) {
            entry.updateDescription(desc);
            entry.setTags(targetTags);
        }

        if (dto.getMood() != null) {
            entry.updateMood(dto.getMood());
        }

        if (dto.getStatus() != null) {
            entry.changeStatus(dto.getStatus());
        }

        if (resolvedMetrics != null) {
            replaceMetrics(entry, resolvedMetrics);
        }

        metricUsageAggService.onEntryCreated(entry);
        tagUsageAggService.onEntryCreated(entry);
        return mapper.toDto(entry);
    }

    @Override
    public void delete(Long id, Long userId) {
        DiaryEntry entry = getEntryGraphForUser(id, userId);
        entryMetricDetailsLoader.loadForEntry(id);

        metricUsageAggService.onEntryDeleted(entry);
        tagUsageAggService.onEntryDeleted(entry);
        entry.markDeleted();
    }

    private void applyMetricsOnCreate(
            List<EntryMetricCreateDto> metrics,
            DiaryEntry entry,
            Set<Tag> tags
    ) {
        if (metrics == null || metrics.isEmpty()) {
            return;
        }

        List<ResolvedMetric> resolvedMetrics = resolveMetricsForCreate(metrics);
        tagMetricService.validateMetricTypesAllowedForTags(tags, metricTypeIds(resolvedMetrics));
        applyResolvedMetrics(entry, resolvedMetrics);
    }

    private List<ResolvedMetric> resolveMetricsForCreate(List<EntryMetricCreateDto> metrics) {
        return resolveMetricsForCreate(metrics, loadDictionaryItems(collectDictionaryIdsForCreate(metrics)));
    }

    private List<ResolvedMetric> resolveMetricsForCreate(
            List<EntryMetricCreateDto> metrics,
            Map<Long, DictionaryItem> dictionaryItems
    ) {
        if (metrics == null || metrics.isEmpty()) {
            return List.of();
        }

        List<ResolvedMetric> resolvedMetrics = new ArrayList<>();
        for (EntryMetricCreateDto dto : metrics) {
            DictionaryItem metricType = resolveDictionary(
                    dictionaryItems,
                    dto.getMetricTypeId(),
                    DictionaryType.METRIC_NAME
            );

            List<ResolvedMetricValue> values = new ArrayList<>();
            for (EntryMetricValueCreateDto valueDto : dto.getValues()) {
                DictionaryItem unit = resolveDictionary(
                        dictionaryItems,
                        valueDto.getUnitId(),
                        DictionaryType.METRIC_UNIT
                );
                values.add(new ResolvedMetricValue(unit, valueDto.getValue()));
            }

            values.sort(Comparator.comparing(value -> value.unit().getId()));
            resolvedMetrics.add(new ResolvedMetric(metricType, values));
        }

        resolvedMetrics.sort(Comparator.comparing(metric -> metric.metricType().getId()));
        return resolvedMetrics;
    }

    private List<ResolvedMetric> resolveMetricsForUpdate(List<EntryMetricUpdateDto> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return List.of();
        }

        Map<Long, DictionaryItem> dictionaryItems = loadDictionaryItems(collectDictionaryIdsForUpdate(metrics));
        List<ResolvedMetric> resolvedMetrics = new ArrayList<>();

        for (EntryMetricUpdateDto dto : metrics) {
            DictionaryItem metricType = resolveDictionary(
                    dictionaryItems,
                    dto.getMetricTypeId(),
                    DictionaryType.METRIC_NAME
            );

            List<ResolvedMetricValue> values = new ArrayList<>();
            for (EntryMetricValueUpdateDto valueDto : dto.getValues().stream()
                    .sorted(Comparator.comparing(EntryMetricValueUpdateDto::getUnitId))
                    .toList()) {
                DictionaryItem unit = resolveDictionary(
                        dictionaryItems,
                        valueDto.getUnitId(),
                        DictionaryType.METRIC_UNIT
                );
                values.add(new ResolvedMetricValue(unit, valueDto.getValue()));
            }

            resolvedMetrics.add(new ResolvedMetric(metricType, values));
        }

        return resolvedMetrics;
    }

    private void replaceMetrics(DiaryEntry entry, List<ResolvedMetric> resolvedMetrics) {
        entry.getMetrics().clear();
        applyResolvedMetrics(entry, resolvedMetrics);
    }

    private void applyResolvedMetrics(DiaryEntry entry, List<ResolvedMetric> resolvedMetrics) {
        for (ResolvedMetric resolvedMetric : resolvedMetrics) {
            EntryMetric metric = EntryMetric.create(entry, resolvedMetric.metricType());
            for (ResolvedMetricValue value : resolvedMetric.values()) {
                metric.addValue(value.unit(), value.value());
            }
            entry.addMetric(metric);
        }
    }

    private Set<Long> collectDictionaryIdsForCreate(List<EntryMetricCreateDto> metrics) {
        Set<Long> ids = new HashSet<>();
        if (metrics == null || metrics.isEmpty()) {
            return ids;
        }

        for (EntryMetricCreateDto metric : metrics) {
            ids.add(metric.getMetricTypeId());
            for (EntryMetricValueCreateDto value : metric.getValues()) {
                ids.add(value.getUnitId());
            }
        }
        return ids;
    }

    private Set<Long> collectDictionaryIdsForUpdate(List<EntryMetricUpdateDto> metrics) {
        Set<Long> ids = new HashSet<>();
        for (EntryMetricUpdateDto metric : metrics) {
            ids.add(metric.getMetricTypeId());
            for (EntryMetricValueUpdateDto value : metric.getValues()) {
                ids.add(value.getUnitId());
            }
        }
        return ids;
    }

    private Map<Long, DictionaryItem> loadDictionaryItems(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        return dictionaryRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(DictionaryItem::getId, item -> item));
    }

    private DictionaryItem resolveDictionary(
            Map<Long, DictionaryItem> dictionaryItems,
            Long id,
            DictionaryType type
    ) {
        DictionaryItem item = dictionaryItems.get(id);
        if (item == null) {
            throw new NotFoundException("Dictionary item not found");
        }

        if (item.getType() != type) {
            throw new BadRequestException("Invalid dictionary type");
        }

        return item;
    }

    private Set<Tag> resolveRequiredTags(Long userId, String description) {
        Set<Tag> resolvedTags = tagResolverService.resolveFromDescription(userId, description);
        if (resolvedTags == null || resolvedTags.isEmpty()) {
            throw new BadRequestException("At least one tag is required");
        }
        return resolvedTags;
    }

    private List<PreparedCreate> prepareCreates(List<DiaryEntryCreateDto> dtos, Long userId) {
        List<PreparedCreateCandidate> candidates = new ArrayList<>(dtos.size());
        Set<String> allTagNames = new LinkedHashSet<>();

        for (DiaryEntryCreateDto dto : dtos) {
            validationService.validateCreate(dto);

            String description = dto.getDescription() == null ? null : dto.getDescription().trim();
            if (description == null || description.isBlank()) {
                throw new BadRequestException("Description is required");
            }

            Set<String> tagNames = diaryDescriptionTagPolicy.extractValidTagNames(description);
            if (tagNames.isEmpty()) {
                throw new BadRequestException("At least one tag is required");
            }

            allTagNames.addAll(tagNames);
            candidates.add(new PreparedCreateCandidate(dto, description, tagNames));
        }

        Map<String, Tag> tagsByName = tagResolverService.resolveForUser(userId, allTagNames).stream()
                .collect(Collectors.toMap(Tag::getName, tag -> tag));

        List<PreparedCreate> preparedCreates = new ArrayList<>(candidates.size());
        for (PreparedCreateCandidate candidate : candidates) {
            Set<Tag> tags = candidate.tagNames().stream()
                    .map(tagsByName::get)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (tags.isEmpty()) {
                throw new BadRequestException("At least one tag is required");
            }

            preparedCreates.add(new PreparedCreate(candidate.dto(), candidate.description(), tags));
        }

        return preparedCreates;
    }

    private List<Long> metricTypeIds(List<ResolvedMetric> metrics) {
        return metrics.stream()
                .map(ResolvedMetric::metricType)
                .map(DictionaryItem::getId)
                .toList();
    }

    private Set<Long> tagIds(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return Set.of();
        }

        return tags.stream()
                .map(Tag::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<Long> currentMetricTypeIds(DiaryEntry entry) {
        return entry.getMetrics().stream()
                .map(EntryMetric::getMetricType)
                .map(DictionaryItem::getId)
                .toList();
    }

    private DiaryEntry getEntryGraphForUser(Long id, Long userId) {
        return diaryRepository.findGraphByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NotFoundException("Entry not found"));
    }

    private record ResolvedMetric(DictionaryItem metricType, List<ResolvedMetricValue> values) {
    }

    private record ResolvedMetricValue(DictionaryItem unit, java.math.BigDecimal value) {
    }

    private record PreparedCreateCandidate(
            DiaryEntryCreateDto dto,
            String description,
            Set<String> tagNames
    ) {
    }

    private record PreparedCreate(
            DiaryEntryCreateDto dto,
            String description,
            Set<Tag> tags
    ) {
    }

    private record MetricValidationKey(
            Set<Long> tagIds,
            List<Long> metricTypeIds
    ) {
    }
}
