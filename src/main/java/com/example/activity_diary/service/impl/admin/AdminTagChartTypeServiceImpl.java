package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.admin.TagChartTypeLinkResponseDto;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.diary.TagChartTypeLink;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.tag.TagChartTypeLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.service.admin.AdminTagChartTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminTagChartTypeServiceImpl implements AdminTagChartTypeService {

    private final TagChartTypeLinkRepository tagChartTypeLinkRepository;
    private final TagRepository tagRepository;

    @Override
    public TagChartTypeLinkResponseDto createLink(Long tagId, ChartType chartType) {
        Tag tag = getTag(tagId);
        ChartType requiredChartType = requireChartType(chartType);

        if (tagChartTypeLinkRepository.existsByTagIdAndChartType(tagId, requiredChartType)) {
            throw new BadRequestException("Tag chart type link already exists");
        }

        try {
            TagChartTypeLink saved = tagChartTypeLinkRepository.saveAndFlush(
                    TagChartTypeLink.create(tag, requiredChartType)
            );

            return toDto(saved.getTag().getId(), saved.getChartType());
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Tag chart type link already exists");
        }
    }

    @Override
    public void deleteLink(Long tagId, ChartType chartType) {
        getTag(tagId);
        ChartType requiredChartType = requireChartType(chartType);

        if (!tagChartTypeLinkRepository.existsByTagIdAndChartType(tagId, requiredChartType)) {
            throw new BadRequestException("Tag chart type link does not exist");
        }

        tagChartTypeLinkRepository.deleteByTagIdAndChartType(tagId, requiredChartType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagChartTypeLinkResponseDto> getChartTypesByTagId(Long tagId) {
        Tag tag = getTag(tagId);

        return tagChartTypeLinkRepository.findChartTypesByTagId(tag.getId()).stream()
                .sorted(Comparator.comparingInt(ChartType::ordinal))
                .map(chartType -> toDto(tag.getId(), chartType))
                .toList();
    }

    private Tag getTag(Long tagId) {
        if (tagId == null) {
            throw new BadRequestException("tagId is required");
        }

        return tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException("Tag not found"));
    }

    private ChartType requireChartType(ChartType chartType) {
        if (chartType == null) {
            throw new BadRequestException("chartType is required");
        }

        return chartType;
    }

    private TagChartTypeLinkResponseDto toDto(Long tagId, ChartType chartType) {
        return TagChartTypeLinkResponseDto.builder()
                .tagId(tagId)
                .chartType(chartType)
                .build();
    }
}
