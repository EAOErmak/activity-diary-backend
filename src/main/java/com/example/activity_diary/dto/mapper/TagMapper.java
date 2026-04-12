package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.entity.diary.Tag;

import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagDto toDto(Tag tag);
    List<TagDto> toDtoList(List<Tag> tags);
}
