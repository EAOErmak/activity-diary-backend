package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.admin.TagMetricLinkResponseDto;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.diary.TagMetricLink;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.tag.TagMetricLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTagMetricLinkServiceImplTest {

    @Mock
    private TagMetricLinkRepository tagMetricLinkRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private DictionaryRepository dictionaryRepository;

    @InjectMocks
    private AdminTagMetricLinkServiceImpl service;

    @Test
    void createLink_savesLink() {
        Tag tag = tag(7L);
        DictionaryItem metricName = dictionaryItem(10L, DictionaryType.METRIC_NAME, "Distance");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(dictionaryRepository.findById(10L)).thenReturn(Optional.of(metricName));
        when(tagMetricLinkRepository.existsByTagIdAndMetricNameId(7L, 10L)).thenReturn(false);
        when(tagMetricLinkRepository.saveAndFlush(any(TagMetricLink.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TagMetricLinkResponseDto result = service.createLink(7L, 10L);

        assertEquals(7L, result.getTagId());
        assertEquals(10L, result.getMetricNameId());
        assertEquals("Distance", result.getMetricNameLabel());

        ArgumentCaptor<TagMetricLink> captor = ArgumentCaptor.forClass(TagMetricLink.class);
        verify(tagMetricLinkRepository).saveAndFlush(captor.capture());
        assertEquals(7L, captor.getValue().getTag().getId());
        assertEquals(10L, captor.getValue().getMetricName().getId());
    }

    @Test
    void createLink_duplicate_throwsBadRequest() {
        Tag tag = tag(7L);
        DictionaryItem metricName = dictionaryItem(10L, DictionaryType.METRIC_NAME, "Distance");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(dictionaryRepository.findById(10L)).thenReturn(Optional.of(metricName));
        when(tagMetricLinkRepository.existsByTagIdAndMetricNameId(7L, 10L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.createLink(7L, 10L));
        verify(tagMetricLinkRepository, never()).saveAndFlush(any(TagMetricLink.class));
    }

    @Test
    void createLink_uniqueConstraintRace_throwsBadRequest() {
        Tag tag = tag(7L);
        DictionaryItem metricName = dictionaryItem(10L, DictionaryType.METRIC_NAME, "Distance");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(dictionaryRepository.findById(10L)).thenReturn(Optional.of(metricName));
        when(tagMetricLinkRepository.existsByTagIdAndMetricNameId(7L, 10L)).thenReturn(false);
        when(tagMetricLinkRepository.saveAndFlush(any(TagMetricLink.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(BadRequestException.class, () -> service.createLink(7L, 10L));
    }

    @Test
    void createLink_wrongDictionaryType_throwsBadRequest() {
        Tag tag = tag(7L);
        DictionaryItem unit = dictionaryItem(10L, DictionaryType.METRIC_UNIT, "kg");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(dictionaryRepository.findById(10L)).thenReturn(Optional.of(unit));

        assertThrows(BadRequestException.class, () -> service.createLink(7L, 10L));
        verify(tagMetricLinkRepository, never()).existsByTagIdAndMetricNameId(7L, 10L);
    }

    @Test
    void deleteLink_existing_deletesLink() {
        Tag tag = tag(7L);
        DictionaryItem metricName = dictionaryItem(10L, DictionaryType.METRIC_NAME, "Distance");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(dictionaryRepository.findById(10L)).thenReturn(Optional.of(metricName));
        when(tagMetricLinkRepository.existsByTagIdAndMetricNameId(7L, 10L)).thenReturn(true);

        service.deleteLink(7L, 10L);

        verify(tagMetricLinkRepository).deleteByTagIdAndMetricNameId(7L, 10L);
    }

    @Test
    void getMetricsByTagId_mapsLinks() {
        Tag tag = tag(7L);
        DictionaryItem metricName = dictionaryItem(10L, DictionaryType.METRIC_NAME, "Distance");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(tagMetricLinkRepository.findByTagId(7L)).thenReturn(List.of(TagMetricLink.create(tag, metricName)));

        List<TagMetricLinkResponseDto> result = service.getMetricsByTagId(7L);

        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().getMetricNameId());
        assertEquals("Distance", result.getFirst().getMetricNameLabel());
    }

    @Test
    void replaceLinks_replacesFullMetricSet() {
        Tag tag = tag(7L);
        DictionaryItem distance = dictionaryItem(10L, DictionaryType.METRIC_NAME, "Distance");
        DictionaryItem weight = dictionaryItem(20L, DictionaryType.METRIC_NAME, "Weight");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(dictionaryRepository.findAllById(java.util.Set.of(20L, 10L))).thenReturn(List.of(weight, distance));
        when(tagMetricLinkRepository.saveAllAndFlush(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<TagMetricLinkResponseDto> result = service.replaceLinks(7L, List.of(20L, 10L));

        assertEquals(List.of(10L, 20L), result.stream().map(TagMetricLinkResponseDto::getMetricNameId).toList());
        verify(tagMetricLinkRepository).deleteByTagId(7L);
        verify(tagMetricLinkRepository).saveAllAndFlush(any());
    }

    @Test
    void replaceLinks_allowsEmptyMetricSet() {
        Tag tag = tag(7L);

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));

        List<TagMetricLinkResponseDto> result = service.replaceLinks(7L, List.of());

        assertEquals(List.of(), result);
        verify(tagMetricLinkRepository).deleteByTagId(7L);
        verify(tagMetricLinkRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void replaceLinks_deduplicatesMetricIds() {
        Tag tag = tag(7L);
        DictionaryItem distance = dictionaryItem(10L, DictionaryType.METRIC_NAME, "Distance");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(dictionaryRepository.findAllById(java.util.Set.of(10L))).thenReturn(List.of(distance));
        when(tagMetricLinkRepository.saveAllAndFlush(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<TagMetricLinkResponseDto> result = service.replaceLinks(7L, List.of(10L, 10L));

        assertEquals(List.of(10L), result.stream().map(TagMetricLinkResponseDto::getMetricNameId).toList());
    }

    @Test
    void replaceLinks_wrongDictionaryType_throwsBadRequest() {
        Tag tag = tag(7L);
        DictionaryItem unit = dictionaryItem(10L, DictionaryType.METRIC_UNIT, "kg");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(dictionaryRepository.findAllById(java.util.Set.of(10L))).thenReturn(List.of(unit));

        assertThrows(BadRequestException.class, () -> service.replaceLinks(7L, List.of(10L)));
        verify(tagMetricLinkRepository, never()).deleteByTagId(7L);
    }

    private static Tag tag(Long id) {
        Tag tag = Tag.builder()
                .name("tag")
                .status(TagStatus.APPROVED)
                .build();
        tag.setId(id);
        return tag;
    }

    private static DictionaryItem dictionaryItem(Long id, DictionaryType type, String label) {
        DictionaryItem item = DictionaryItem.builder()
                .type(type)
                .label(label)
                .active(true)
                .build();
        item.setId(id);
        return item;
    }
}
