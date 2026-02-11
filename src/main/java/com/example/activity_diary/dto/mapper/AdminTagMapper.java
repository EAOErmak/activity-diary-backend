package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.admin.AdminTagDto;
import com.example.activity_diary.entity.diary.Tag;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminTagMapper {

    @Mapping(target = "createdByUserId", source = "createdBy.id")
    AdminTagDto toDto(Tag tag);
}
