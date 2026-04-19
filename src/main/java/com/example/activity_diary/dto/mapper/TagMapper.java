package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.entity.diary.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TagMapper {

    @Mapping(target = "name", expression = "java(toApiTagName(tag.getName()))")
    TagDto toDto(Tag tag);

    List<TagDto> toDtoList(List<Tag> tags);

    default String toApiTagName(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        return name.startsWith("#") ? name : "#" + name;
    }
}
