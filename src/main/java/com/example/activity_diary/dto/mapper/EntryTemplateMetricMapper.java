package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.template.diary.EntryTemplateMetricValueViewDto;
import com.example.activity_diary.dto.template.diary.EntryTemplateMetricViewDto;
import com.example.activity_diary.entity.template.EntryTemplateMetric;
import com.example.activity_diary.entity.template.EntryTemplateMetricValue;

import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EntryTemplateMetricMapper {

    // View metric
    @Mapping(source = "metricType.id", target = "metricTypeId")
    @Mapping(source = "metricType.label", target = "metricTypeName")
    EntryTemplateMetricViewDto toViewDto(EntryTemplateMetric entity);
    List<EntryTemplateMetricViewDto> toViewDtos(List<EntryTemplateMetric> entities);

    // View value
    @Mapping(source = "unit.id", target = "unitId")
    @Mapping(source = "unit.label", target = "unitName")
    EntryTemplateMetricValueViewDto toValueViewDto(EntryTemplateMetricValue entity);
    List<EntryTemplateMetricValueViewDto> toValueViewDtos(List<EntryTemplateMetricValue> entities);
}
