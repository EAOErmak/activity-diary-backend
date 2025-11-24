package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.mapper.DiaryEntryMapper;
import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.repository.DiaryRepository;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.service.diary.*;
import com.example.activity_diary.service.dictionary.DictionaryResolver;
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

    private final DiaryValidationService validationService;
    private final DiaryAccessService accessService;

    private final DiaryItemService itemProcessor;
    private final DiaryCalculationService calculationService;
    private final DictionaryResolver dictionaryResolver;

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

        // validate base fields
        validationService.validateCreate(dto);

        // map base fields
        DiaryEntry entry = mapper.toEntity(dto);
        entry.setUser(user);

        // resolve dictionary ids → dict entities
        entry.setWhatHappened(dictionaryResolver.getWhatHappened(dto.getWhatHappenedId()));
        entry.setWhat(dictionaryResolver.getWhat(dto.getWhatId()));

        // normalize dates
        calculationService.normalizeEntry(dto, entry);

        // set status
        if (dto.getStatus() != null) {
            entry.setStatus(EntryStatus.valueOf(dto.getStatus().toUpperCase()));
        } else if (entry.getStatus() == null) {
            entry.setStatus(EntryStatus.ACTIVE);
        }

        calculationService.computeDuration(entry);

        // NEW ITEMS
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

        // update dictionary fields if provided
        if (dto.getWhatHappenedId() != null) {
            existing.setWhatHappened(dictionaryResolver.getWhatHappened(dto.getWhatHappenedId()));
        }

        if (dto.getWhatId() != null) {
            existing.setWhat(dictionaryResolver.getWhat(dto.getWhatId()));
        }

        // apply other changes
        calculationService.normalizeEntry(dto, existing);

        if (dto.getStatus() != null) {
            existing.setStatus(EntryStatus.valueOf(dto.getStatus().toUpperCase()));
        }

        calculationService.computeDuration(existing);

        // UPDATE ITEMS (NOW USING NEW DTOs)
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
