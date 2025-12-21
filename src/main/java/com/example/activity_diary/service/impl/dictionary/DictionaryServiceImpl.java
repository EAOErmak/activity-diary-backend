// src/main/java/com/example/activity_diary/service/impl/dictionary/DictionaryServiceImpl.java
package com.example.activity_diary.service.impl.dictionary;

import com.example.activity_diary.dto.dictionary.DictionaryCreateDto;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryUpdateDto;
import com.example.activity_diary.dto.mapper.DictionaryMapper;
import com.example.activity_diary.entity.EntryFieldConfig;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.GlobalSyncEntityType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.entity.enums.UserSyncEntityType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.DictionaryRepository;
import com.example.activity_diary.repository.EntryFieldConfigRepository;
import com.example.activity_diary.service.dictionary.DictionaryService;
import com.example.activity_diary.service.sync.GlobalSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DictionaryServiceImpl implements DictionaryService {

    private final DictionaryRepository dictionaryRepository;
    private final EntryFieldConfigRepository entryFieldConfigRepository;
    private final DictionaryMapper mapper;
    private final GlobalSyncService globalSyncService;

    // ============================
    // CREATE
    // ============================

    @Override
    public DictionaryResponseDto create(DictionaryCreateDto dto) {

        if (dto.getType() == null)
            throw new BadRequestException("Dictionary type is required");

        if (dto.getLabel() == null || dto.getLabel().trim().isEmpty())
            throw new BadRequestException("Label is required");

        if (dto.getType() == DictionaryType.CATEGORY && dto.getChartType() == null)
            throw new BadRequestException("chartType is required for CATEGORY");

        if (dto.getType() == DictionaryType.CATEGORY && dto.getEntryFieldConfigId() == null)
            throw new BadRequestException("EntryFieldConfig is required for CATEGORY");

        String cleanLabel = dto.getLabel().trim();

        if (dictionaryRepository.existsByTypeAndLabelIgnoreCase(dto.getType(), cleanLabel))
            throw new BadRequestException("Dictionary item already exists");

        DictionaryItem parent = null;

        if (dto.getType() == DictionaryType.SUB_CATEGORY) {
            if (dto.getParentId() == null)
                throw new BadRequestException("parentId is required for SUB_CATEGORY");

            parent = dictionaryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent not found"));

            if (parent.getType() != DictionaryType.CATEGORY)
                throw new BadRequestException("SUB_CATEGORY can be linked only to CATEGORY");
        }

        EntryFieldConfig config = null;

        if (dto.getType() == DictionaryType.CATEGORY) {
            config = entryFieldConfigRepository.findById(dto.getEntryFieldConfigId())
                    .orElseThrow(() -> new NotFoundException("EntryFieldConfig not found"));
        }

        DictionaryItem item = DictionaryItem.builder()
                .type(dto.getType())
                .label(cleanLabel)
                .allowedRole(dto.getAllowedRole())
                .chartType(dto.getChartType())
                .active(true)
                .parent(parent)
                .entryFieldConfig(config) // ✅ ВОТ ТУТ ГЛАВНОЕ ИЗМЕНЕНИЕ
                .build();

        globalSyncService.bump(GlobalSyncEntityType.DICTIONARY);

        DictionaryItem saved = dictionaryRepository.save(item);

        return mapper.toDto(saved);
    }

    // ============================
    // READ (USER)
    // ============================

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryResponseDto> getForUser(
            DictionaryType type,
            Long parentId,
            Role role
    ) {
        if (type == DictionaryType.SUB_CATEGORY && parentId == null) {
            throw new BadRequestException("parentId is required for type WHAT");
        }

        return dictionaryRepository
                .findByTypeAndVisibleForUser(type, parentId, role)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryResponseDto> getAll(Role role){
        return dictionaryRepository
                .findAllVisibleForUser(role)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // ============================
    // READ (ADMIN)
    // ============================

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryResponseDto> getByTypeForAdmin(DictionaryType type) {
        return dictionaryRepository
                .findAllByTypeOrderByLabelAsc(type)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // ============================
    // UPDATE
    // ============================

    @Override
    public DictionaryResponseDto update(Long id, DictionaryUpdateDto dto) {

        DictionaryItem item = dictionaryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dictionary item not found"));

        if (dto.getLabel() != null) {
            String clean = dto.getLabel().trim();

            if (clean.isEmpty()) {
                throw new BadRequestException("Label cannot be empty");
            }

            boolean exists = dictionaryRepository
                    .existsByTypeAndLabelIgnoreCaseAndIdNot(
                            item.getType(),
                            clean,
                            item.getId()
                    );

            if (exists) {
                throw new BadRequestException("Dictionary item with this label already exists");
            }

            item.setLabel(clean);
        }

        if (dto.getActive() != null) {
            item.setActive(dto.getActive());
        }

        if (dto.getAllowedRole() != null) {
            item.setAllowedRole(dto.getAllowedRole());
        }

        globalSyncService.bump(GlobalSyncEntityType.DICTIONARY);

        return mapper.toDto(dictionaryRepository.save(item));
    }

    // ============================
    // SEARCH (USER)
    // ============================

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryResponseDto> search(String query, Role role) {

        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        return dictionaryRepository
                .searchVisibleForUser(query.trim(), role.name())
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // ============================
    // SEARCH (ADMIN)
    // ============================

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryResponseDto> searchForAdmin(String query) {

        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        return dictionaryRepository
                .findAllByLabelContainingIgnoreCaseOrderByTypeAscLabelAsc(query.trim())
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}

