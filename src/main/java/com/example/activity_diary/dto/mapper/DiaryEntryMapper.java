package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.diary.*;
import com.example.activity_diary.dto.diary.metric.EntryMetricResponseDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueResponseDto;
import com.example.activity_diary.entity.EntryMetric;
import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.EntryMetricValue;
import org.mapstruct.*;
import org.springframework.data.domain.Slice;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface DiaryEntryMapper {

    /* ======================================================
       DiaryEntry → DiaryEntryDto
       ====================================================== */

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.label", target = "categoryName")

    @Mapping(source = "subCategory.id", target = "subCategoryId")
    @Mapping(source = "subCategory.label", target = "subCategoryName")

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "metrics", target = "metrics")

    DiaryEntryDto toDto(DiaryEntry entry);

    /* ======================================================
       Create / Update
       ====================================================== */

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subCategory", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "metrics", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DiaryEntry toEntity(DiaryEntryCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subCategory", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "metrics", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget DiaryEntry entry, DiaryEntryUpdateDto dto);

    /* ======================================================
       EntryMetric → EntryMetricResponseDto
       ====================================================== */

    @Mapping(source = "metricType.id", target = "metricTypeId")
    @Mapping(source = "metricType.label", target = "metricTypeName")
    @Mapping(source = "values", target = "values")
    EntryMetricResponseDto toMetricResponseDto(EntryMetric metric);

    List<EntryMetricResponseDto> toMetricResponseDtoList(List<EntryMetric> metrics);

    /* ======================================================
       EntryMetricValue → EntryMetricValueResponseDto
       ====================================================== */

    @Mapping(source = "unit.id", target = "unitId")
    @Mapping(source = "unit.label", target = "unitName")
    @Mapping(source = "value", target = "value")
    EntryMetricValueResponseDto toMetricValueResponseDto(EntryMetricValue value);

    List<EntryMetricValueResponseDto> toMetricValueResponseDtoList(
            List<EntryMetricValue> values
    );

    /* ======================================================
       Lightweight View DTO
       ====================================================== */

    @Mapping(source = "category.label", target = "categoryName")
    @Mapping(source = "subCategory.label", target = "subCategoryName")
    DiaryEntryViewDto toListDto(DiaryEntry entity);

    List<DiaryEntryViewDto> toListDtoList(List<DiaryEntry> entities);

    default Slice<DiaryEntryViewDto> toListDtoSlice(Slice<DiaryEntry> slice) {
        return slice.map(this::toListDto);
    }
}
