package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.dto.mapper.TagMapper;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.repository.tag.TagChartTypeLinkRepository;
import com.example.activity_diary.repository.tag.TagMetricLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private TagChartTypeLinkRepository tagChartTypeLinkRepository;

    @Mock
    private TagMetricLinkRepository tagMetricLinkRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    void getVisibleTags_withoutQuery_returnsAllVisibleTags() {
        List<Tag> tags = List.of(Tag.builder().name("sport").status(TagStatus.APPROVED).build());
        List<TagDto> expected = List.of(new TagDto());

        when(tagRepository.findAllVisible(7L)).thenReturn(tags);
        when(tagMapper.toDtoList(tags)).thenReturn(expected);

        List<TagDto> actual = tagService.getVisibleTags(7L, Role.USER, null);

        assertSame(expected, actual);
        verify(tagRepository).findAllVisible(7L);
        verify(tagMapper).toDtoList(tags);
    }

    @Test
    void getVisibleTags_withQuery_searchesVisibleTags() {
        List<Tag> tags = List.of(Tag.builder().name("sport").status(TagStatus.APPROVED).build());
        List<TagDto> expected = List.of(new TagDto());

        when(tagRepository.searchVisible(7L, "sport")).thenReturn(tags);
        when(tagMapper.toDtoList(tags)).thenReturn(expected);

        List<TagDto> actual = tagService.getVisibleTags(7L, Role.USER, "  Sport  ");

        assertSame(expected, actual);
        verify(tagRepository).searchVisible(7L, "sport");
        verify(tagMapper).toDtoList(tags);
    }

    @Test
    void approve_updatesStatus() {
        Tag tag = Tag.builder().status(TagStatus.PROPOSED).build();
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        tagService.approve(1L);

        assertEquals(TagStatus.APPROVED, tag.getStatus());
    }

    @Test
    void reject_updatesStatus() {
        Tag tag = Tag.builder().status(TagStatus.PROPOSED).build();
        when(tagRepository.findById(2L)).thenReturn(Optional.of(tag));

        tagService.reject(2L);

        assertEquals(TagStatus.REJECTED, tag.getStatus());
    }

    @Test
    void deprecate_updatesStatus() {
        Tag tag = Tag.builder().status(TagStatus.PROPOSED).build();
        when(tagRepository.findById(3L)).thenReturn(Optional.of(tag));

        tagService.deprecate(3L);

        assertEquals(TagStatus.DEPRECATED, tag.getStatus());
    }

    @Test
    void approve_whenTagMissing_throwsNotFound() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tagService.approve(99L));
    }

    @Test
    void deleteTag_whenTagMissing_throwsNotFound() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tagService.deleteTag(99L));

        verify(diaryRepository, never()).existsByTags_Id(99L);
        verify(tagRepository, never()).delete(any());
    }

    @Test
    void deleteTag_whenDiaryEntryReferencesTag_throwsBadRequest() {
        Tag tag = Tag.builder().status(TagStatus.APPROVED).build();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(diaryRepository.existsByTags_Id(1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> tagService.deleteTag(1L));

        verify(tagChartTypeLinkRepository, never()).existsByTagId(1L);
        verify(tagRepository, never()).delete(tag);
    }

    @Test
    void deleteTag_whenChartTypeLinkReferencesTag_throwsBadRequest() {
        Tag tag = Tag.builder().status(TagStatus.APPROVED).build();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(diaryRepository.existsByTags_Id(1L)).thenReturn(false);
        when(tagChartTypeLinkRepository.existsByTagId(1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> tagService.deleteTag(1L));

        verify(tagRepository, never()).delete(tag);
    }

    @Test
    void deleteTag_whenMetricLinkReferencesTag_throwsBadRequest() {
        Tag tag = Tag.builder().status(TagStatus.APPROVED).build();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(diaryRepository.existsByTags_Id(1L)).thenReturn(false);
        when(tagChartTypeLinkRepository.existsByTagId(1L)).thenReturn(false);
        when(tagMetricLinkRepository.existsByTagId(1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> tagService.deleteTag(1L));

        verify(tagRepository, never()).delete(tag);
    }

    @Test
    void deleteTag_whenNoUnsafeReferences_deletesTag() {
        Tag tag = Tag.builder().status(TagStatus.APPROVED).build();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(diaryRepository.existsByTags_Id(1L)).thenReturn(false);
        when(tagChartTypeLinkRepository.existsByTagId(1L)).thenReturn(false);
        when(tagMetricLinkRepository.existsByTagId(1L)).thenReturn(false);

        tagService.deleteTag(1L);

        verify(tagRepository).delete(tag);
        verify(tagRepository).flush();
    }

    @Test
    void deleteTag_whenDatabaseRejectsDelete_throwsBadRequest() {
        Tag tag = Tag.builder().status(TagStatus.APPROVED).build();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(diaryRepository.existsByTags_Id(1L)).thenReturn(false);
        when(tagChartTypeLinkRepository.existsByTagId(1L)).thenReturn(false);
        when(tagMetricLinkRepository.existsByTagId(1L)).thenReturn(false);
        doThrow(new DataIntegrityViolationException("constraint"))
                .when(tagRepository)
                .flush();

        assertThrows(BadRequestException.class, () -> tagService.deleteTag(1L));

        verify(tagRepository).delete(tag);
    }
}
