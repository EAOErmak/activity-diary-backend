package com.example.activity_diary.service.admin;

import com.example.activity_diary.dto.admin.MetricLinkRequestDto;
import com.example.activity_diary.dto.admin.MetricLinkResponseDto;

import java.util.List;

public interface AdminMetricLinkService {

    MetricLinkResponseDto createLink(Long metricNameId, Long metricUnitId);

    default MetricLinkResponseDto createLink(MetricLinkRequestDto dto) {
        return createLink(dto.getMetricNameId(), dto.getMetricUnitId());
    }

    void deleteLink(Long metricNameId, Long metricUnitId);

    List<MetricLinkResponseDto> getUnitsByMetricName(Long metricNameId);
}
