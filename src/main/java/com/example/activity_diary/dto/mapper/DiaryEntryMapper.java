package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.diary.*;
import com.example.activity_diary.dto.diary.metric.EntryMetricResponseDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueResponseDto;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.diary.EntryMetricValue;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.service.impl.diary.DiaryDescriptionTagPolicy;

import org.mapstruct.*;
import org.springframework.data.domain.Slice;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface DiaryEntryMapper {

    //DiaryEntry в†’ DiaryEntryDto
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "metrics", target = "metrics")
    DiaryEntryDto toDto(DiaryEntry entry);

    //Create / Update
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "metrics", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DiaryEntry toEntity(DiaryEntryCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "metrics", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget DiaryEntry entry, DiaryEntryUpdateDto dto);

    //EntryMetric в†’ EntryMetricResponseDto
    @Mapping(source = "metricType.id", target = "metricTypeId")
    @Mapping(source = "metricType.label", target = "metricTypeName")
    @Mapping(source = "values", target = "values")
    EntryMetricResponseDto toMetricResponseDto(EntryMetric metric);

    default List<EntryMetricResponseDto> toMetricResponseDtoList(List<EntryMetric> metrics) {
        if (metrics == null) return null;
        return metrics.stream()
                .sorted(Comparator.comparing(metric -> metric.getMetricType().getId()))
                .map(this::toMetricResponseDto)
                .toList();
    }

    //EntryMetricValue в†’ EntryMetricValueResponseDto
    @Mapping(source = "unit.id", target = "unitId")
    @Mapping(source = "unit.label", target = "unitName")
    @Mapping(source = "value", target = "value")
    EntryMetricValueResponseDto toMetricValueResponseDto(EntryMetricValue value);

    default List<EntryMetricValueResponseDto> toMetricValueResponseDtoList(
            List<EntryMetricValue> values
    ) {
        if (values == null) return null;
        return values.stream()
                .sorted(Comparator.comparing(value -> value.getUnit().getId()))
                .map(this::toMetricValueResponseDto)
                .toList();
    }

    //Lightweight View DTO
    @Mapping(target = "firstTag", expression = "java(firstTag(entity.getTags()))")
    DiaryEntryViewDto toListDto(DiaryEntry entity);

    List<DiaryEntryViewDto> toListDtoList(List<DiaryEntry> entities);

    default Slice<DiaryEntryViewDto> toListDtoSlice(Slice<DiaryEntry> slice) {
        return slice.map(this::toListDto);
    }

    default List<String> map(Set<Tag> tags) {
        if (tags == null) return List.of();
        return tags.stream()
                .map(Tag::getName)
                .map(this::toApiTagName)
                .toList();
    }

    default String firstTag(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) return null;

        // "РїРµСЂРІС‹Р№" РґРµР»Р°РµРј РґРµС‚РµСЂРјРёРЅРёСЂРѕРІР°РЅРЅС‹Рј: РјРёРЅРёРјР°Р»СЊРЅС‹Р№ tag.id
        return tags.stream()
                .filter(t -> t != null && t.getId() != null)
                .min(java.util.Comparator.comparing(Tag::getId))
                .map(Tag::getName)
                .map(this::toApiTagName)
                .orElse(null);
    }

    default String toApiTagName(String name) {
        if (name == null || name.isBlank()) return name;
        return DiaryDescriptionTagPolicy.normalizeCanonicalTagName(name);
    }
}
