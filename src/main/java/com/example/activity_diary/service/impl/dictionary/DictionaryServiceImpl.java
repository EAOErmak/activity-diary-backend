// src/main/java/com/example/activity_diary/service/impl/dictionary/DictionaryServiceImpl.java
package com.example.activity_diary.service.impl.dictionary;

import com.example.activity_diary.dto.dictionary.DictionaryCreateDto;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryUpdateDto;
import com.example.activity_diary.dto.mapper.DictionaryMapper;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.DictionaryRepository;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.service.dictionary.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DictionaryServiceImpl implements DictionaryService {

    private final DictionaryRepository dictionaryRepository;
    private final UserRepository userRepository;
    private final DictionaryMapper mapper;

    // ============================
    // CREATE
    // ============================

    @Override
    public DictionaryResponseDto create(DictionaryCreateDto dto) {

        if (dto.getType() == null) {
            throw new BadRequestException("Dictionary type is required");
        }

        if (dto.getLabel() == null || dto.getLabel().trim().isEmpty()) {
            throw new BadRequestException("Label is required");
        }

        String cleanLabel = dto.getLabel().trim();

        if (dictionaryRepository.existsByTypeAndLabelIgnoreCase(dto.getType(), cleanLabel)) {
            throw new BadRequestException("Dictionary item already exists");
        }

        DictionaryItem item = DictionaryItem.builder()
                .type(dto.getType())
                .label(cleanLabel)
                .allowedRole(dto.getAllowedRole())
                .active(true)
                .build();

        return mapper.toDto(dictionaryRepository.save(item));
    }

    // ============================
    // READ (USER)
    // ============================

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryResponseDto> getByType(DictionaryType type, UserDetails ud) {

        User user = userRepository.findByUsername(ud.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return dictionaryRepository
                .findAllByTypeAndActiveTrueOrderByLabelAsc(type)
                .stream()
                .filter(item -> hasAccess(item, user))
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

        return mapper.toDto(dictionaryRepository.save(item));
    }

    // ============================
    // SEARCH (USER)
    // ============================

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryResponseDto> search(String query, UserDetails ud) {

        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        User user = userRepository.findByUsername(ud.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return dictionaryRepository
                .findAllByLabelContainingIgnoreCaseAndActiveTrueOrderByTypeAscLabelAsc(query.trim())
                .stream()
                .filter(item -> hasAccess(item, user))
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

    // ============================
    // ACCESS CONTROL
    // ============================

    private boolean hasAccess(DictionaryItem item, User user) {

        if (item.getAllowedRole() == null) return true;

        if (user.getRole() == Role.ADMIN) return true;

        return item.getAllowedRole().equals(user.getRole().name());
    }
}
