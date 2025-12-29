package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.EntryMetricCreateDto;
import com.example.activity_diary.dto.diary.EntryMetricUpdateDto;
import com.example.activity_diary.entity.EntryMetric;
import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.DictionaryRepository;
import com.example.activity_diary.service.diary.DiaryItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryItemServiceImpl implements DiaryItemService {

    private final DictionaryRepository dictionaryRepository;

    @Override
    public void applyOnCreate(List<EntryMetricCreateDto> dtos, DiaryEntry entry) {

        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        for (EntryMetricCreateDto dto : dtos) {
            EntryMetric item = createItem(
                    entry,
                    dto.getMetricId(),
                    dto.getUnitId(),
                    dto.getValue()
            );
            entry.addMetric(item);
        }
    }

    @Override
    public void applyOnUpdate(List<EntryMetricUpdateDto> dtos, DiaryEntry entry) {

        if (dtos == null) {
            return;
        }

        List<EntryMetric> existing = new ArrayList<>(entry.getMetrics());
        for (EntryMetric item : existing) {
            entry.removeMetric(item);
        }

        if (dtos.isEmpty()) {
            return;
        }

        for (EntryMetricUpdateDto dto : dtos) {
            EntryMetric item = createItem(
                    entry,
                    dto.getMetricId(),
                    dto.getUnitId(),
                    dto.getValue()
            );
            entry.addMetric(item);
        }
    }

    private EntryMetric createItem(
            DiaryEntry entry,
            Long nameId,
            Long unitId,
            Integer count
    ) {
        DictionaryItem name = resolveDictionary(nameId, DictionaryType.METRIC_NAME);
        DictionaryItem unit = resolveDictionary(unitId, DictionaryType.METRIC_UNIT);

        return EntryMetric.create(
                entry,
                name,
                unit,
                count
        );
    }

    private DictionaryItem resolveDictionary(Long id, DictionaryType type) {

        DictionaryItem item = dictionaryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dictionary item not found: " + id));

        if (item.getType() != type) {
            throw new BadRequestException("Invalid dictionary type: " + type);
        }

        return item;
    }
}
