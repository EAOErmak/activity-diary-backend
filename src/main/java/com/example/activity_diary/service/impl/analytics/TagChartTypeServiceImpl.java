package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.tag.TagChartTypeLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.service.analytics.TagChartTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagChartTypeServiceImpl implements TagChartTypeService {

    private final TagChartTypeLinkRepository tagChartTypeLinkRepository;
    private final TagRepository tagRepository;

    @Override
    public List<ChartType> getChartTypesByTagId(Long tagId) {
        getTag(tagId);

        return tagChartTypeLinkRepository.findChartTypesByTagId(tagId).stream()
                .sorted(Comparator.comparingInt(ChartType::ordinal))
                .toList();
    }

    @Override
    public void validateChartTypeAllowed(Long tagId, ChartType chartType) {
        if (chartType == null) {
            throw new BadRequestException("chartType is required");
        }

        getTag(tagId);

        if (!tagChartTypeLinkRepository.existsByTagIdAndChartType(tagId, chartType)) {
            throw new BadRequestException("Chart type " + chartType + " is not allowed for tag " + tagId);
        }
    }

    private Tag getTag(Long tagId) {
        if (tagId == null) {
            throw new BadRequestException("tagId is required");
        }

        return tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException("Tag not found"));
    }
}
