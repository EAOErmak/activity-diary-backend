package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryViewDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.mapper.DiaryEntryMapper;
import com.example.activity_diary.dto.diary.metric.EntryMetricCreateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricUpdateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueCreateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueUpdateDto;
import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.EntryMetric;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.DiaryRepository;
import com.example.activity_diary.repository.DictionaryRepository;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.service.analytics.TagUsageAggService;
import com.example.activity_diary.service.diary.*;
import com.example.activity_diary.service.sync.UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.activity_diary.entity.enums.UserSyncEntityType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class DiaryServiceImpl implements DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final DictionaryRepository dictionaryRepository;

    private final DiaryValidationService validationService;
    private final UserSyncService userSyncService;
    private final TagResolverService tagResolverService;
    private final DiaryEntryMapper mapper;
    private final TagUsageAggService tagUsageAggService;

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
    @Transactional(readOnly = true)
    public List<DiaryEntryViewDto> getEntriesByDateRange(
            Long userId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new BadRequestException("Invalid date range");
        }

        return diaryRepository
                .findByUserAndDateRange(
                        userId,
                        from,
                        to
                )
                .stream()
                .map(mapper::toListDto)
                .toList();
    }

    @Override
    public DiaryEntryDto getMyEntryById(Long id, Long userId) {

        DiaryEntry entry = diaryRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Entry not found"));

        return mapper.toDto(entry);
    }

    @Override
    public DiaryEntryDto create(DiaryEntryCreateDto dto, Long userId) {

        validationService.validateCreate(dto);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        DiaryEntry entry = DiaryEntry.create(
                user,
                dto.getWhenStarted(),
                dto.getWhenEnded(),
                dto.getMood(),
                dto.getDescription()
        );

        applyMetricsOnCreate(dto.getMetrics(), entry);

        entry.setTags(tagResolverService.resolveFromDescription(userId, dto.getDescription()));

        DiaryEntry saved = diaryRepository.save(entry);

        tagUsageAggService.onEntryCreated(saved);

        userSyncService.bump(userId, UserSyncEntityType.DIARY);

        return mapper.toDto(saved);
    }

    @Override
    public DiaryEntryDto update(Long id, DiaryEntryUpdateDto dto, Long userId) {

        DiaryEntry entry = diaryRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Entry not found"));

        if (entry.getWhenEnded().isBefore(Instant.now())) {
            throw new BadRequestException("Past entry cannot be modified");
        }

        if (dto.getWhenStarted() != null && dto.getWhenEnded() != null) {
            entry.updateTime(dto.getWhenStarted(), dto.getWhenEnded());
        }

        if (dto.getDescription() != null) {
            entry.updateDescription(dto.getDescription());
        }

        if (dto.getMood() != null) {
            entry.updateMood(dto.getMood());
        }

        if (dto.getStatus() != null) {
            entry.changeStatus(dto.getStatus());
        }

        if (dto.getMetrics() != null) {
            replaceMetrics(entry, dto.getMetrics());
        }

        if (dto.getDescription() != null) {
            entry.setTags(tagResolverService.resolveFromDescription(userId, dto.getDescription()));
        }

        DiaryEntry saved = diaryRepository.save(entry);

        userSyncService.bump(userId, UserSyncEntityType.DIARY);

        return mapper.toDto(saved);
    }

    @Override
    public void delete(Long id, Long userId) {

        DiaryEntry entry = diaryRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Entry not found"));

        entry.markDeleted();

        diaryRepository.save(entry);

        userSyncService.bump(userId, UserSyncEntityType.DIARY);
    }

    private void applyMetricsOnCreate(
            List<EntryMetricCreateDto> metrics,
            DiaryEntry entry
    ) {
        if (metrics == null || metrics.isEmpty()) return;

        for (EntryMetricCreateDto dto : metrics) {

            DictionaryItem metricType = resolveDictionary(
                    dto.getMetricTypeId(),
                    DictionaryType.METRIC_NAME
            );

            EntryMetric metric = EntryMetric.create(entry, metricType);

            for (EntryMetricValueCreateDto valueDto : dto.getValues()) {

                DictionaryItem unit = resolveDictionary(
                        valueDto.getUnitId(),
                        DictionaryType.METRIC_UNIT
                );

                metric.addValue(unit, valueDto.getValue());
            }

            entry.addMetric(metric);
        }
    }

    private void replaceMetrics(
            DiaryEntry entry,
            List<EntryMetricUpdateDto> metrics
    ) {
        entry.getMetrics().clear();

        for (EntryMetricUpdateDto dto : metrics) {

            DictionaryItem metricType = resolveDictionary(
                    dto.getMetricTypeId(),
                    DictionaryType.METRIC_NAME
            );

            EntryMetric metric = EntryMetric.create(entry, metricType);

            for (EntryMetricValueUpdateDto valueDto : dto.getValues()) {

                DictionaryItem unit = resolveDictionary(
                        valueDto.getUnitId(),
                        DictionaryType.METRIC_UNIT
                );

                metric.addValue(unit, valueDto.getValue());
            }

            entry.addMetric(metric);
        }
    }

    private DictionaryItem resolveDictionary(Long id, DictionaryType type) {

        DictionaryItem item = dictionaryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dictionary item not found"));

        if (item.getType() != type) {
            throw new BadRequestException("Invalid dictionary type");
        }

        return item;
    }
}
