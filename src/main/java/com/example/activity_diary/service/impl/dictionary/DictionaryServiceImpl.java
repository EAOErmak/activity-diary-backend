// src/main/java/com/example/activity_diary/service/impl/dictionary/DictionaryServiceImpl.java
package com.example.activity_diary.service.impl.dictionary;

import com.example.activity_diary.dto.PageResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryCreateDto;
import com.example.activity_diary.dto.dictionary.DictionaryOptionDto;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryUpdateDto;
import com.example.activity_diary.dto.mapper.DictionaryMapper;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.diary.MetricNameUnitLinkRepository;
import com.example.activity_diary.service.dictionary.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DictionaryServiceImpl implements DictionaryService {

    private final DictionaryRepository dictionaryRepository;
    private final MetricNameUnitLinkRepository metricNameUnitLinkRepository;
    private final DictionaryMapper mapper;
    @Override
    public DictionaryResponseDto create(DictionaryCreateDto dto) {

        if (dto.getType() == null)
            throw new BadRequestException("Dictionary type is required");

        if (dto.getLabel() == null || dto.getLabel().trim().isEmpty())
            throw new BadRequestException("Label is required");

        String cleanLabel = dto.getLabel().trim();

        if (dictionaryRepository.existsByTypeAndLabelIgnoreCase(dto.getType(), cleanLabel))
            throw new BadRequestException("Dictionary item already exists");

        DictionaryItem item = DictionaryItem.builder()
                .type(dto.getType())
                .label(cleanLabel)
                .allowedRole(dto.getAllowedRole())
                .active(true)
                .build();

        DictionaryItem saved = dictionaryRepository.save(item);

        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryResponseDto> getForUser(
            DictionaryType type,
            Role role
    ) {
        return dictionaryRepository
                .findByTypeAndVisibleForUser(type, role)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryOptionDto> getUnitsByMetricNameId(Long metricNameId, Role role) {
        DictionaryItem metricName = dictionaryRepository.findById(metricNameId)
                .orElseThrow(() -> new NotFoundException("Dictionary item not found"));

        if (metricName.getType() != DictionaryType.METRIC_NAME) {
            throw new BadRequestException("Dictionary item is not a metric name");
        }

        return metricNameUnitLinkRepository.findUnitsByMetricNameId(metricNameId).stream()
                .filter(item -> isVisibleForRole(item, role))
                .map(mapper::toOptionDto)
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

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<DictionaryResponseDto> getByTypeForAdmin(DictionaryType type, String q, Pageable pageable) {
        String query = normalizeQuery(q);
        Page<DictionaryResponseDto> page = dictionaryRepository.findAdminPageByType(type, query, pageable)
                .map(mapper::toDto);
        return PageResponseDto.from(page);
    }

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

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        return query.trim();
    }

    private boolean isVisibleForRole(DictionaryItem item, Role role) {
        return item.getAllowedRole() == null || item.getAllowedRole() == role;
    }
}
