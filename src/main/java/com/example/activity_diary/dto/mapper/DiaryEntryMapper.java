package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.diary.*;
import com.example.activity_diary.dto.diary.metric.EntryMetricResponseDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueResponseDto;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.EntryMetric;
import com.example.activity_diary.entity.diary.EntryMetricValue;
import com.example.activity_diary.entity.diary.Tag;

import org.mapstruct.*;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface DiaryEntryMapper {

    //DiaryEntry → DiaryEntryDto
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "metrics", target = "metrics")
    DiaryEntryDto toDto(DiaryEntry entry);

    //Create / Update
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "metrics", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DiaryEntry toEntity(DiaryEntryCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "metrics", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget DiaryEntry entry, DiaryEntryUpdateDto dto);

    //EntryMetric → EntryMetricResponseDto
    @Mapping(source = "metricType.id", target = "metricTypeId")
    @Mapping(source = "metricType.label", target = "metricTypeName")
    @Mapping(source = "values", target = "values")
    EntryMetricResponseDto toMetricResponseDto(EntryMetric metric);

    List<EntryMetricResponseDto> toMetricResponseDtoList(List<EntryMetric> metrics);

    //EntryMetricValue → EntryMetricValueResponseDto
    @Mapping(source = "unit.id", target = "unitId")
    @Mapping(source = "unit.label", target = "unitName")
    @Mapping(source = "value", target = "value")
    EntryMetricValueResponseDto toMetricValueResponseDto(EntryMetricValue value);

    List<EntryMetricValueResponseDto> toMetricValueResponseDtoList(
            List<EntryMetricValue> values
    );

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
                .toList();
    }

    default String firstTag(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) return null;

        // "первый" делаем детерминированным: минимальный tag.id
        return tags.stream()
                .filter(t -> t != null && t.getId() != null)
                .min(java.util.Comparator.comparing(Tag::getId))
                .map(Tag::getName)
                .orElse(null);
    }
}
