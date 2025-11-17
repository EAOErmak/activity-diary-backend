package com.example.activity_diary.dto.mappers;

import com.example.activity_diary.dto.*;
import com.example.activity_diary.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DiaryEntryMapper {

    @Mapping(source = "user.id", target = "userId")
    DiaryEntryDto toDto(DiaryEntry entry);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "whatDidYouDo", ignore = true)
    DiaryEntry toEntity(DiaryEntryCreateDto dto);

    // Helper for list items (optional)
    ActivityItem toActivityItem(ActivityItemDto dto);
}
