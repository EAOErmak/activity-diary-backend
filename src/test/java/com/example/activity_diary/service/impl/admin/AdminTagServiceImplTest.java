package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.diary.TagCreateDto;
import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.dto.diary.TagUpdateDto;
import com.example.activity_diary.dto.mapper.TagMapper;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.repository.tag.TagChartTypeLinkRepository;
import com.example.activity_diary.repository.tag.TagMetricLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.repository.tag.TagUsageAggRepository;
import com.example.activity_diary.repository.tag.UserTagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private TagMetricLinkRepository tagMetricLinkRepository;

    @Mock
    private TagChartTypeLinkRepository tagChartTypeLinkRepository;

    @Mock
    private UserTagRepository userTagRepository;

    @Mock
    private TagUsageAggRepository tagUsageAggRepository;

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

    @Test
    void update_existingTag_changesNameAndReturnsDto() {
        TagUpdateDto dto = updateDto("workout");
        Tag tag = existingTag(1L, "sport");
        TagDto expected = new TagDto();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.findByName("workout")).thenReturn(Optional.empty());
        when(tagRepository.save(same(tag))).thenReturn(tag);
        when(tagMapper.toDto(tag)).thenReturn(expected);

        TagDto actual = service.update(1L, dto);

        assertSame(expected, actual);
        assertEquals("workout", tag.getName());
        verify(tagRepository).findById(1L);
        verify(tagRepository).findByName("workout");
        verify(tagRepository).save(tag);
        verify(tagMapper).toDto(tag);
    }

    @Test
    void update_trimsAndLowercasesTagName() {
        TagUpdateDto dto = updateDto(" Workout ");
        Tag tag = existingTag(1L, "sport");
        TagDto expected = new TagDto();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.findByName("workout")).thenReturn(Optional.empty());
        when(tagRepository.save(same(tag))).thenReturn(tag);
        when(tagMapper.toDto(tag)).thenReturn(expected);

        TagDto actual = service.update(1L, dto);

        assertSame(expected, actual);
        assertEquals("workout", tag.getName());
        verify(tagRepository).findByName("workout");
    }

    @Test
    void update_leadingHashtagIsRemoved() {
        TagUpdateDto dto = updateDto("#Workout");
        Tag tag = existingTag(1L, "sport");
        TagDto expected = new TagDto();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.findByName("workout")).thenReturn(Optional.empty());
        when(tagRepository.save(same(tag))).thenReturn(tag);
        when(tagMapper.toDto(tag)).thenReturn(expected);

        TagDto actual = service.update(1L, dto);

        assertSame(expected, actual);
        assertEquals("workout", tag.getName());
        verify(tagRepository).findByName("workout");
    }

    @Test
    void update_duplicateTag_throwsBadRequestWhenNameBelongsToAnotherTag() {
        TagUpdateDto dto = updateDto("sport");
        Tag currentTag = existingTag(2L, "workout");
        Tag existingTag = existingTag(1L, "sport");

        when(tagRepository.findById(2L)).thenReturn(Optional.of(currentTag));
        when(tagRepository.findByName("sport")).thenReturn(Optional.of(existingTag));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.update(2L, dto));

        assertEquals("Tag already exists", exception.getMessage());
        verify(tagRepository).findById(2L);
        verify(tagRepository).findByName("sport");
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void update_sameNameForCurrentTag_doesNotFailDuplicateCheck() {
        TagUpdateDto dto = updateDto(" Sport ");
        Tag tag = existingTag(1L, "sport");
        TagDto expected = new TagDto();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.findByName("sport")).thenReturn(Optional.of(tag));
        when(tagRepository.save(same(tag))).thenReturn(tag);
        when(tagMapper.toDto(tag)).thenReturn(expected);

        TagDto actual = service.update(1L, dto);

        assertSame(expected, actual);
        assertEquals("sport", tag.getName());
        verify(tagRepository).save(tag);
    }

    @Test
    void update_nonExistingTag_throwsNotFound() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.update(99L, updateDto("workout")));

        assertEquals("Tag not found", exception.getMessage());
        verify(tagRepository).findById(99L);
        verify(tagRepository, never()).findByName(any());
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void delete_existingTag_removesDependenciesAndDeletesTag() {
        Tag tag = existingTag(7L, "sport");
        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));

        service.delete(7L);

        InOrder inOrder = org.mockito.Mockito.inOrder(
                diaryRepository,
                tagMetricLinkRepository,
                tagChartTypeLinkRepository,
                userTagRepository,
                tagUsageAggRepository,
                tagRepository
        );
        inOrder.verify(diaryRepository).deleteTagLinksByTagId(7L);
        inOrder.verify(tagMetricLinkRepository).deleteByTagId(7L);
        inOrder.verify(tagChartTypeLinkRepository).deleteByTagId(7L);
        inOrder.verify(userTagRepository).deleteByTagId(7L);
        inOrder.verify(tagUsageAggRepository).deleteByTagId(7L);
        inOrder.verify(tagRepository).delete(tag);
        inOrder.verify(tagRepository).flush();
    }

    @Test
    void delete_nonExistingTag_throwsNotFound() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.delete(99L));

        assertEquals("Tag not found", exception.getMessage());
        verify(diaryRepository, never()).deleteTagLinksByTagId(99L);
        verify(tagMetricLinkRepository, never()).deleteByTagId(99L);
        verify(tagChartTypeLinkRepository, never()).deleteByTagId(99L);
        verify(userTagRepository, never()).deleteByTagId(99L);
        verify(tagUsageAggRepository, never()).deleteByTagId(99L);
        verify(tagRepository, never()).delete(any(Tag.class));
    }

    @Test
    void delete_whenDatabaseStillRejectsDelete_throwsBadRequest() {
        Tag tag = existingTag(7L, "sport");
        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("constraint"))
                .when(tagRepository)
                .flush();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> service.delete(7L));

        assertEquals("Tag cannot be deleted because it is still referenced", exception.getMessage());
        verify(diaryRepository).deleteTagLinksByTagId(7L);
        verify(tagMetricLinkRepository).deleteByTagId(7L);
        verify(tagChartTypeLinkRepository).deleteByTagId(7L);
        verify(userTagRepository).deleteByTagId(7L);
        verify(tagUsageAggRepository).deleteByTagId(7L);
        verify(tagRepository).delete(tag);
        verify(tagRepository).flush();
    }

    private static TagCreateDto dto(String name) {
        TagCreateDto dto = new TagCreateDto();
        dto.setName(name);
        return dto;
    }

    private static TagUpdateDto updateDto(String name) {
        TagUpdateDto dto = new TagUpdateDto();
        dto.setName(name);
        return dto;
    }

    private static Tag existingTag(Long id, String name) {
        Tag tag = Tag.builder()
                .name(name)
                .status(TagStatus.APPROVED)
                .build();
        tag.setId(id);
        return tag;
    }
}
