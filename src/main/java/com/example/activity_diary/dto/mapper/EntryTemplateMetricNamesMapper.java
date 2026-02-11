package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.template.*;
import com.example.activity_diary.dto.template.diary.EntryTemplateMetricValueViewDto;
import com.example.activity_diary.dto.template.diary.EntryTemplateMetricViewDto;
import com.example.activity_diary.entity.template.EntryTemplateMetric;
import com.example.activity_diary.entity.template.EntryTemplateMetricValue;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntryTemplateMetricNamesMapper {

    @Mapping(target = "metricTypeId", expression = "java(entity.getMetricType() == null ? null : entity.getMetricType().getId())")
    @Mapping(target = "metricTypeName", expression = "java(entity.getMetricType() == null ? null : entity.getMetricType().getLabel())")
    EntryTemplateMetricViewDto toViewDto(EntryTemplateMetric entity);

    @Mapping(target = "unitId", expression = "java(entity.getUnit() == null ? null : entity.getUnit().getId())")
    @Mapping(target = "unitName", expression = "java(entity.getUnit() == null ? null : entity.getUnit().getLabel())")
    EntryTemplateMetricValueViewDto toValueViewDto(EntryTemplateMetricValue entity);
}
