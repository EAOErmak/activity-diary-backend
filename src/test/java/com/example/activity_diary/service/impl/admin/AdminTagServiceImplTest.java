package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.diary.TagCreateDto;
import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.dto.mapper.TagMapper;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.repository.tag.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private AdminTagServiceImpl service;

    @Test
    void create_savesApprovedTagFromPlainName() {
        TagCreateDto dto = dto("sport");

        Tag saved = Tag.builder()
                .name("sport")
                .status(TagStatus.APPROVED)
                .build();
        TagDto expected = new TagDto();

        when(tagRepository.findByName("sport")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenReturn(saved);
        when(tagMapper.toDto(saved)).thenReturn(expected);

        TagDto actual = service.create(dto);

        assertSame(expected, actual);
        verify(tagRepository).findByName("sport");
        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(tagCaptor.capture());
        assertEquals("sport", tagCaptor.getValue().getName());
        assertEquals(TagStatus.APPROVED, tagCaptor.getValue().getStatus());
        verify(tagMapper).toDto(saved);
    }

    @Test
    void create_trimsAndLowercasesTagName() {
        TagCreateDto dto = dto(" Tag ");

        Tag saved = Tag.builder()
                .name("tag")
                .status(TagStatus.APPROVED)
                .build();
        TagDto expected = new TagDto();

        when(tagRepository.findByName("tag")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenReturn(saved);
        when(tagMapper.toDto(saved)).thenReturn(expected);

        TagDto actual = service.create(dto);

        assertSame(expected, actual);
        verify(tagRepository).findByName("tag");
        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(tagCaptor.capture());
        assertEquals("tag", tagCaptor.getValue().getName());
    }

    @Test
    void create_duplicateTag_throwsBadRequestAfterTrimAndLowercase() {
        TagCreateDto dto = dto(" Sport ");

        when(tagRepository.findByName("sport")).thenReturn(Optional.of(Tag.builder().name("sport").build()));

        assertThrows(BadRequestException.class, () -> service.create(dto));
        verify(tagRepository).findByName("sport");
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void create_nameWithHashtag_throwsBadRequest() {
        TagCreateDto dto = dto("#sport");

        assertThrows(BadRequestException.class, () -> service.create(dto));
        verify(tagRepository, never()).findByName(any());
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void create_nameWithInternalHashtag_throwsBadRequest() {
        TagCreateDto dto = dto("ta#g");

        assertThrows(BadRequestException.class, () -> service.create(dto));
        verify(tagRepository, never()).findByName(any());
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void create_doesNotDependOnDiaryDescriptionTagPolicy() {
        boolean hasDescriptionPolicyField = Arrays.stream(AdminTagServiceImpl.class.getDeclaredFields())
                .anyMatch(field -> field.getType().getSimpleName().equals("DiaryDescriptionTagPolicy"));

        assertFalse(hasDescriptionPolicyField);
    }

    private static TagCreateDto dto(String name) {
        TagCreateDto dto = new TagCreateDto();
        dto.setName(name);
        return dto;
    }
}
