package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.diary.*;
import com.example.activity_diary.entity.EntryMetric;
import com.example.activity_diary.entity.DiaryEntry;
import org.mapstruct.*;
import org.springframework.data.domain.Slice;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface DiaryEntryMapper {

    // ===========================
    //      ENTITY → RESPONSE DTO
    // ===========================

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.label", target = "categoryName")

    @Mapping(source = "subCategory.id", target = "subCategoryId")
    @Mapping(source = "subCategory.label", target = "subCategoryName")

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "metrics", target = "metrics")

    @Mapping(source = "mood", target = "mood")
    @Mapping(source = "description", target = "description")
    DiaryEntryDto toDto(DiaryEntry entry);

    // ===========================
    //      CREATE DTO → ENTITY
    // ===========================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)

    @Mapping(target = "subCategory", ignore = true)
    @Mapping(target = "category", ignore = true)

    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)

    @Mapping(target = "metrics", ignore = true)
    DiaryEntry toEntity(DiaryEntryCreateDto dto);

    // ========================================
    //      UPDATE DTO → ENTITY (partial)
    // ========================================

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)

    @Mapping(target = "subCategory", ignore = true)
    @Mapping(target = "category", ignore = true)

    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "metrics", ignore = true)

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget DiaryEntry entry, DiaryEntryUpdateDto dto);

    // ========================================
    //   ENTRY METRIC → RESPONSE DTO
    // ========================================

    @Mapping(source = "metricType.id", target = "metricTypeId")
    @Mapping(source = "metricType.label", target = "metricTypeName")

    @Mapping(source = "unit.id", target = "unitId")
    @Mapping(source = "unit.label", target = "unitName")

    @Mapping(source = "value", target = "value")
    EntryMetricResponseDto toMetricResponseDto(EntryMetric item);

    List<EntryMetricResponseDto> toMetricResponseDtoList(List<EntryMetric> list);

    @Mapping(source = "category.label", target = "categoryName")
    @Mapping(source = "subCategory.label", target = "subCategoryName")
    DiaryEntryViewDto toListDto(DiaryEntry entity);

    List<DiaryEntryViewDto> toListDtoList(List<DiaryEntry> entities);

    default Slice<DiaryEntryViewDto> toListDtoSlice(Slice<DiaryEntry> slice) {
        return slice.map(this::toListDto);
    }
}
