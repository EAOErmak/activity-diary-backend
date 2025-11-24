package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.diary.*;
import com.example.activity_diary.entity.ActivityItem;
import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.dict.ActivityItemNameDict;
import com.example.activity_diary.entity.dict.ActivityItemUnitDict;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true) // <-- FIX: disable Lombok builder
)
public interface DiaryEntryMapper {

    // ===========================
    //      ENTITY → RESPONSE DTO
    // ===========================
    @Mapping(source = "whatHappened.id", target = "whatHappenedId")
    @Mapping(source = "whatHappened.name", target = "whatHappenedName")

    @Mapping(source = "what.id", target = "whatId")
    @Mapping(source = "what.name", target = "whatName")

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "whatDidYouDo", target = "whatDidYouDo")
    DiaryEntryDto toDto(DiaryEntry entry);


    // ===========================
    //      CREATE DTO → ENTITY
    // ===========================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "what", ignore = true)
    @Mapping(target = "whatHappened", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "whatDidYouDo", ignore = true)
    DiaryEntry toEntity(DiaryEntryCreateDto dto);


    // ========================================
    //      UPDATE DTO → ENTITY (partial)
    // ========================================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "what", ignore = true)
    @Mapping(target = "whatHappened", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "whatDidYouDo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget DiaryEntry entry, DiaryEntryUpdateDto dto);


    // ========================================
    //   ACTIVITY ITEM MAPPING
    // ========================================

    // Entity → Response DTO
    @Mapping(source = "name.id", target = "nameId")
    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "unit.id", target = "unitId")
    @Mapping(source = "unit.name", target = "unit")
    ActivityItemResponseDto toActivityItemResponseDto(ActivityItem item);

    List<ActivityItemResponseDto> toActivityItemResponseDtoList(List<ActivityItem> list);


    // Create DTO → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "diaryEntry", ignore = true)
    @Mapping(source = "nameId", target = "name")
    @Mapping(source = "unitId", target = "unit")
    ActivityItem toActivityItem(ActivityItemCreateDto dto);

    List<ActivityItem> toActivityItemList(List<ActivityItemCreateDto> list);


    // ========================================
    //  ID → DICT ENTITY STUB
    // ========================================
    default ActivityItemNameDict mapName(Long id) {
        if (id == null) return null;
        ActivityItemNameDict d = new ActivityItemNameDict();
        d.setId(id);
        return d;
    }

    default ActivityItemUnitDict mapUnit(Long id) {
        if (id == null) return null;
        ActivityItemUnitDict d = new ActivityItemUnitDict();
        d.setId(id);
        return d;
    }
}
