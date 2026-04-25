package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.TagStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TagMapperTest {

    private final TagMapper mapper = Mappers.getMapper(TagMapper.class);

    @Test
    void toDto_returnsCanonicalNameWithoutHash() {
        Tag tag = Tag.builder()
                .name("sport")
                .status(TagStatus.APPROVED)
                .build();
        tag.setId(1L);

        TagDto dto = mapper.toDto(tag);

        assertEquals("sport", dto.getName());
    }
}
