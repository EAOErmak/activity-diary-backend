package com.example.activity_diary.service.admin;

import com.example.activity_diary.dto.PageResponseDto;
import com.example.activity_diary.dto.admin.MetricLinkRequestDto;
import com.example.activity_diary.dto.admin.MetricLinkResponseDto;

public interface AdminMetricLinkService {

    MetricLinkResponseDto createLink(Long metricNameId, Long metricUnitId);

    default MetricLinkResponseDto createLink(MetricLinkRequestDto dto) {
        return createLink(dto.getMetricNameId(), dto.getMetricUnitId());
    }

    void deleteLink(Long metricNameId, Long metricUnitId);

    PageResponseDto<MetricLinkResponseDto> getUnitsByMetricName(Long metricNameId, int page, int limit);
}
