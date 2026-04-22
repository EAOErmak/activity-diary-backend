package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.admin.TagMetricLinkResponseDto;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.diary.TagMetricLink;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.tag.TagMetricLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.service.admin.AdminTagMetricLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminTagMetricLinkServiceImpl implements AdminTagMetricLinkService {

    private final TagMetricLinkRepository tagMetricLinkRepository;
    private final TagRepository tagRepository;
    private final DictionaryRepository dictionaryRepository;

    @Override
    public TagMetricLinkResponseDto createLink(Long tagId, Long metricNameId) {
        Tag tag = getTag(tagId);
        DictionaryItem metricName = getMetricName(metricNameId);

        if (tagMetricLinkRepository.existsByTagIdAndMetricNameId(tagId, metricNameId)) {
            throw new BadRequestException("Tag metric link already exists");
        }

        try {
            TagMetricLink saved = tagMetricLinkRepository.saveAndFlush(
                    TagMetricLink.create(tag, metricName)
            );

            return toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Tag metric link already exists");
        }
    }

    @Override
    public void deleteLink(Long tagId, Long metricNameId) {
        getTag(tagId);
        getMetricName(metricNameId);

        if (!tagMetricLinkRepository.existsByTagIdAndMetricNameId(tagId, metricNameId)) {
            throw new BadRequestException("Tag metric link does not exist");
        }

        tagMetricLinkRepository.deleteByTagIdAndMetricNameId(tagId, metricNameId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagMetricLinkResponseDto> getMetricsByTagId(Long tagId) {
        getTag(tagId);

        return tagMetricLinkRepository.findByTagId(tagId).stream()
                .map(this::toDto)
                .toList();
    }

    private Tag getTag(Long tagId) {
        if (tagId == null) {
            throw new BadRequestException("tagId is required");
        }

        return tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException("Tag not found"));
    }

    private DictionaryItem getMetricName(Long metricNameId) {
        if (metricNameId == null) {
            throw new BadRequestException("metricNameId is required");
        }

        DictionaryItem metricName = dictionaryRepository.findById(metricNameId)
                .orElseThrow(() -> new BadRequestException("Metric name not found"));

        if (metricName.getType() != DictionaryType.METRIC_NAME) {
            throw new BadRequestException("Metric name must be of type METRIC_NAME");
        }

        return metricName;
    }

    private TagMetricLinkResponseDto toDto(TagMetricLink link) {
        return TagMetricLinkResponseDto.builder()
                .tagId(link.getTag().getId())
                .metricNameId(link.getMetricName().getId())
                .metricNameLabel(link.getMetricName().getLabel())
                .build();
    }
}
