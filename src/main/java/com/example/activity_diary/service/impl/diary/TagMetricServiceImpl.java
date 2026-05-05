package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.PageResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryOptionDto;
import com.example.activity_diary.dto.mapper.DictionaryMapper;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.tag.TagMetricLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.service.diary.TagMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagMetricServiceImpl implements TagMetricService {

    private final TagMetricLinkRepository tagMetricLinkRepository;
    private final TagRepository tagRepository;
    private final DictionaryMapper dictionaryMapper;

    @Override
    public List<DictionaryOptionDto> getMetricsByTagId(Long tagId, Long userId, Role role) {
        Set<Long> visibleTagIds = getVisibleTagIds(List.of(requireTagId(tagId)), userId, role);
        return tagMetricLinkRepository.findVisibleMetricNamesByTagIds(visibleTagIds, role).stream()
                .map(dictionaryMapper::toOptionDto)
                .toList();
    }

    @Override
    public PageResponseDto<DictionaryOptionDto> getMetricsByTagIds(
            Collection<Long> tagIds,
            Long userId,
            Role role,
            String q,
            Pageable pageable
    ) {
        Set<Long> visibleTagIds = getVisibleTagIds(tagIds, userId, role);
        String query = normalizeQuery(q);
        Page<DictionaryOptionDto> page = (query == null
                ? tagMetricLinkRepository.findVisibleMetricNamesPageByTagIds(visibleTagIds, role, pageable)
                : tagMetricLinkRepository.findVisibleMetricNamesPageByTagIdsAndLabelSearch(visibleTagIds, role, query, pageable))
                .map(dictionaryMapper::toOptionDto);
        return PageResponseDto.from(page);
    }

    @Override
    public void validateMetricTypesAllowedForTags(Set<Tag> tags, Collection<Long> metricTypeIds) {
        if (metricTypeIds == null || metricTypeIds.isEmpty()) {
            return;
        }

        if (tags == null || tags.isEmpty()) {
            throw new BadRequestException("At least one tag is required when metrics are provided");
        }

        Set<Long> tagIds = tags.stream()
                .filter(Objects::nonNull)
                .map(Tag::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (tagIds.isEmpty()) {
            throw new BadRequestException("At least one tag is required when metrics are provided");
        }

        Set<Long> allowedMetricTypeIds = tagMetricLinkRepository.findMetricNameIdsByTagIds(tagIds);

        metricTypeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .filter(metricTypeId -> !allowedMetricTypeIds.contains(metricTypeId))
                .findFirst()
                .ifPresent(metricTypeId -> {
                    throw new BadRequestException(
                            "Metric " + metricTypeId + " is not allowed for selected tag"
                    );
                });
    }

    private Set<Long> getVisibleTagIds(Collection<Long> tagIds, Long userId, Role role) {
        Set<Long> requiredTagIds = requireTagIds(tagIds);
        List<Tag> tags = tagRepository.findAllById(requiredTagIds);

        Set<Long> visibleTagIds = tags.stream()
                .filter(tag -> role == Role.ADMIN || isVisibleToUser(tag, userId))
                .map(Tag::getId)
                .collect(Collectors.toSet());

        if (visibleTagIds.size() != requiredTagIds.size()) {
            throw new NotFoundException("Tag not found");
        }

        return visibleTagIds;
    }

    private Set<Long> requireTagIds(Collection<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new BadRequestException("At least one tag is required");
        }

        Set<Long> requiredTagIds = new LinkedHashSet<>();
        for (Long tagId : tagIds) {
            requiredTagIds.add(requireTagId(tagId));
        }

        return requiredTagIds;
    }

    private Long requireTagId(Long tagId) {
        if (tagId == null) {
            throw new BadRequestException("tagId is required");
        }

        return tagId;
    }

    private boolean isVisibleToUser(Tag tag, Long userId) {
        if (tag.getStatus() == TagStatus.APPROVED) {
            return true;
        }

        Long createdById = tag.getCreatedBy() == null ? null : tag.getCreatedBy().getId();
        return tag.getStatus() == TagStatus.PENDING && Objects.equals(createdById, userId);
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        return query.trim().toLowerCase(Locale.ROOT);
    }
}
