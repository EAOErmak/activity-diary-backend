package com.example.activity_diary.service.admin;

import com.example.activity_diary.dto.admin.TagMetricLinkRequestDto;
import com.example.activity_diary.dto.admin.TagMetricLinkReplaceRequestDto;
import com.example.activity_diary.dto.admin.TagMetricLinkResponseDto;

import java.util.Collection;
import java.util.List;

public interface AdminTagMetricLinkService {

    TagMetricLinkResponseDto createLink(Long tagId, Long metricNameId);

    default TagMetricLinkResponseDto createLink(TagMetricLinkRequestDto dto) {
        return createLink(dto.getTagId(), dto.getMetricNameId());
    }

    void deleteLink(Long tagId, Long metricNameId);

    List<TagMetricLinkResponseDto> getMetricsByTagId(Long tagId);

    List<TagMetricLinkResponseDto> replaceLinks(Long tagId, Collection<Long> metricNameIds);

    default List<TagMetricLinkResponseDto> replaceLinks(Long tagId, TagMetricLinkReplaceRequestDto dto) {
        return replaceLinks(tagId, dto.getMetricNameIds());
    }
}
