package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.template.*;
import com.example.activity_diary.entity.EntryTemplateMetric;
import com.example.activity_diary.entity.EntryTemplateMetricValue;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EntryTemplateMetricMapper {

    // View metric
    EntryTemplateMetricViewDto toViewDto(EntryTemplateMetric entity);
    List<EntryTemplateMetricViewDto> toViewDtos(List<EntryTemplateMetric> entities);

    // View value
    EntryTemplateMetricValueViewDto toValueViewDto(EntryTemplateMetricValue entity);
    List<EntryTemplateMetricValueViewDto> toValueViewDtos(List<EntryTemplateMetricValue> entities);
}

