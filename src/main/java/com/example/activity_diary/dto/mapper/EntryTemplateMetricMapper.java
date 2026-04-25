package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.template.diary.EntryTemplateMetricValueViewDto;
import com.example.activity_diary.dto.template.diary.EntryTemplateMetricViewDto;
import com.example.activity_diary.entity.template.EntryTemplateMetric;
import com.example.activity_diary.entity.template.EntryTemplateMetricValue;

import org.mapstruct.*;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EntryTemplateMetricMapper {

    // View metric
    @Mapping(source = "metricType.id", target = "metricTypeId")
    @Mapping(source = "metricType.label", target = "metricTypeName")
    EntryTemplateMetricViewDto toViewDto(EntryTemplateMetric entity);

    default List<EntryTemplateMetricViewDto> toViewDtos(List<EntryTemplateMetric> entities) {
        if (entities == null) return null;
        return entities.stream()
                .sorted(Comparator.comparing(metric -> metric.getMetricType().getId()))
                .map(this::toViewDto)
                .toList();
    }

    // View value
    @Mapping(source = "unit.id", target = "unitId")
    @Mapping(source = "unit.label", target = "unitName")
    EntryTemplateMetricValueViewDto toValueViewDto(EntryTemplateMetricValue entity);

    default List<EntryTemplateMetricValueViewDto> toValueViewDtos(List<EntryTemplateMetricValue> entities) {
        if (entities == null) return null;
        return entities.stream()
                .sorted(Comparator.comparing(value -> value.getUnit().getId()))
                .map(this::toValueViewDto)
                .toList();
    }
}
