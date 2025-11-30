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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryServiceImpl implements DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final DictionaryRepository dictionaryRepository;

    private final DiaryValidationService validationService;
    private final DiaryAccessService accessService;
    private final DiaryItemService itemProcessor;
    private final DiaryCalculationService calculationService;

    private final DiaryEntryMapper mapper;

    // ============================================================
    // GET PAGED ENTRIES FOR CURRENT USER
    // ============================================================

    @Override
    public Page<DiaryEntryDto> getMyEntries(UserDetails currentUser, int page, int size) {

        Long userId = accessService.getUserId(currentUser);

        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, size),
                Sort.by("whenStarted").descending()
        );

        Page<DiaryEntry> entries = diaryRepository.findByUserId(userId, pageable);

        return entries.map(mapper::toDto);
    }

    // ============================================================
    // GET SINGLE ENTRY
    // ============================================================

    @Override
    public DiaryEntryDto getMyEntryById(Long id, UserDetails currentUser) {
        DiaryEntry entry = accessService.getEntryForUser(id, currentUser);
        return mapper.toDto(entry);
    }

    // ============================================================
    // CREATE ENTRY
    // ============================================================

    @Override
    public DiaryEntryDto create(DiaryEntryCreateDto dto, UserDetails currentUser) {

        Long userId = accessService.getUserId(currentUser);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // ✅ Валидация
        validationService.validateCreate(dto);

        // ✅ Базовый маппинг
        DiaryEntry entry = mapper.toEntity(dto);
        entry.setUser(user);

        // ✅ Разрешение WHAT_HAPPENED
        DictionaryItem whatHappened = dictionaryRepository
                .findById(dto.getWhatHappenedId())
                .orElseThrow(() -> new NotFoundException("WHAT_HAPPENED not found"));

        if (whatHappened.getType() != DictionaryType.WHAT_HAPPENED) {
            throw new BadRequestException("Invalid dictionary type for WHAT_HAPPENED");
        }

        // ✅ Разрешение WHAT
        DictionaryItem what = dictionaryRepository
                .findById(dto.getWhatId())
                .orElseThrow(() -> new NotFoundException("WHAT not found"));

        if (what.getType() != DictionaryType.WHAT) {
            throw new BadRequestException("Invalid dictionary type for WHAT");
        }

        entry.setWhatHappened(whatHappened);
        entry.setWhat(what);

        // ✅ Нормализация и длительность
        calculationService.normalizeEntry(dto, entry);

        if (dto.getStatus() != null) {
            try {
                entry.setStatus(EntryStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid entry status");
            }
        } else {
            entry.setStatus(EntryStatus.ACTIVE);
        }

        calculationService.computeDuration(entry);

        // ✅ ITEMS
        itemProcessor.applyItems(dto.getWhatDidYouDo(), entry);

        DiaryEntry saved = diaryRepository.save(entry);

        return mapper.toDto(saved);
    }

    // ============================================================
    // UPDATE ENTRY
    // ============================================================

    @Override
    public DiaryEntryDto update(Long id, DiaryEntryUpdateDto dto, UserDetails currentUser) {

        DiaryEntry existing = accessService.getEntryForUser(id, currentUser);

        // ✅ Обновление WHAT_HAPPENED
        if (dto.getWhatHappenedId() != null) {

            DictionaryItem whatHappened = dictionaryRepository
                    .findById(dto.getWhatHappenedId())
                    .orElseThrow(() -> new NotFoundException("WHAT_HAPPENED not found"));

            if (whatHappened.getType() != DictionaryType.WHAT_HAPPENED) {
                throw new BadRequestException("Invalid dictionary type for WHAT_HAPPENED");
            }

            existing.setWhatHappened(whatHappened);
        }

        // ✅ Обновление WHAT
        if (dto.getWhatId() != null) {

            DictionaryItem what = dictionaryRepository
                    .findById(dto.getWhatId())
                    .orElseThrow(() -> new NotFoundException("WHAT not found"));

            if (what.getType() != DictionaryType.WHAT) {
                throw new BadRequestException("Invalid dictionary type for WHAT");
            }

            existing.setWhat(what);
        }

        // ✅ Нормализация и статус
        calculationService.normalizeEntry(dto, existing);

        if (dto.getStatus() != null) {
            try {
                existing.setStatus(EntryStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid entry status");
            }
        }

        calculationService.computeDuration(existing);

        // ✅ ITEMS
        itemProcessor.updateItems(dto.getWhatDidYouDo(), existing);

        DiaryEntry saved = diaryRepository.save(existing);

        return mapper.toDto(saved);
    }

    // ============================================================
    // DELETE ENTRY
    // ============================================================

    @Override
    public void delete(Long id, UserDetails currentUser) {

        DiaryEntry existing = accessService.getEntryForUser(id, currentUser);

        diaryRepository.delete(existing);
    }
}
