package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.mapper.DiaryEntryMapper;
import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.DiaryRepository;
import com.example.activity_diary.repository.DictionaryRepository;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.service.diary.*;
import com.example.activity_diary.service.sync.UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.activity_diary.entity.enums.SyncEntityType;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryServiceImpl implements DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final DictionaryRepository dictionaryRepository;

    private final DiaryValidationService validationService;
    private final DiaryItemService itemService;
    private final UserSyncService userSyncService;
    private final DiaryEntryMapper mapper;

    // ============================================================
    // GET PAGED
    // ============================================================

    @Override
    public Page<DiaryEntryDto> getMyEntries(Long userId, Pageable pageable) {
        return diaryRepository
                .findByUserIdAndStatusNot(userId, EntryStatus.DELETED, pageable)
                .map(mapper::toDto);
    }

    // ============================================================
    // GET ONE
    // ============================================================

    @Override
    public DiaryEntryDto getMyEntryById(Long id, Long userId) {

        DiaryEntry entry = diaryRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Entry not found"));

        return mapper.toDto(entry);
    }

    // ============================================================
    // CREATE
    // ============================================================

    @Override
    public DiaryEntryDto create(DiaryEntryCreateDto dto, Long userId) {

        validationService.validateCreate(dto);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        DictionaryItem category = resolveDictionary(
                dto.getCategoryId(),
                DictionaryType.CATEGORY
        );

        DictionaryItem subCategory = resolveDictionary(
                dto.getSubCategoryId(),
                DictionaryType.SUB_CATEGORY
        );

        DiaryEntry entry = DiaryEntry.create(
                user,
                category,
                subCategory,
                dto.getWhenStarted(),
                dto.getWhenEnded(),
                dto.getMood(),
                dto.getDescription()
        );

        itemService.applyOnCreate(dto.getMetrics(), entry);

        DiaryEntry saved = diaryRepository.save(entry);

        userSyncService.bump(userId, SyncEntityType.DIARY);

        return mapper.toDto(saved);
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @Override
    public DiaryEntryDto update(Long id, DiaryEntryUpdateDto dto, Long userId) {

        DiaryEntry entry = diaryRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Entry not found"));

        if (entry.getWhenEnded().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Past entry cannot be modified");
        }

        if (dto.getCategoryId() != null) {
            entry = updateCategory(entry, dto.getCategoryId());
        }

        if (dto.getSubCategoryId() != null) {
            entry = updateSubCategory(entry, dto.getSubCategoryId());
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

        itemService.applyOnUpdate(dto.getMetrics(), entry);

        DiaryEntry saved = diaryRepository.save(entry);

        userSyncService.bump(userId, SyncEntityType.DIARY);

        return mapper.toDto(saved);
    }

    // ============================================================
    // DELETE (LOGICAL)
    // ============================================================

    @Override
    public void delete(Long id, Long userId) {

        DiaryEntry entry = diaryRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Entry not found"));

        entry.markDeleted();

        diaryRepository.save(entry);

        userSyncService.bump(userId, SyncEntityType.DIARY);
    }

    // ============================================================
    // INTERNAL HELPERS
    // ============================================================

    private DictionaryItem resolveDictionary(Long id, DictionaryType type) {

        DictionaryItem item = dictionaryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dictionary item not found"));

        if (item.getType() != type) {
            throw new BadRequestException("Invalid dictionary type");
        }

        return item;
    }

    private DiaryEntry updateCategory(DiaryEntry entry, Long id) {
        entry.changeCategory(resolveDictionary(id, DictionaryType.CATEGORY));
        return entry;
    }

    private DiaryEntry updateSubCategory(DiaryEntry entry, Long id) {
        entry.changeSubCategory(resolveDictionary(id, DictionaryType.SUB_CATEGORY));
        return entry;
    }
}
