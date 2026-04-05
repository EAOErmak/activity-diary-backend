package com.example.activity_diary.service.admin;

import com.example.activity_diary.dto.admin.TagChartTypeLinkRequestDto;
import com.example.activity_diary.dto.admin.TagChartTypeLinkResponseDto;
import com.example.activity_diary.entity.enums.ChartType;

import java.util.List;

public interface AdminTagChartTypeService {

    TagChartTypeLinkResponseDto createLink(Long tagId, ChartType chartType);

    default TagChartTypeLinkResponseDto createLink(TagChartTypeLinkRequestDto dto) {
        return createLink(dto.getTagId(), dto.getChartType());
    }

    void deleteLink(Long tagId, ChartType chartType);

    List<TagChartTypeLinkResponseDto> getChartTypesByTagId(Long tagId);
}
