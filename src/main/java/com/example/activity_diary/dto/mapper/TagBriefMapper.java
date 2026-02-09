package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.template.*;
import com.example.activity_diary.entity.*;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface TagBriefMapper {
    TagBriefDto toBriefDto(Tag tag);
    Set<TagBriefDto> toBriefDtos(Set<Tag> tags);
}
