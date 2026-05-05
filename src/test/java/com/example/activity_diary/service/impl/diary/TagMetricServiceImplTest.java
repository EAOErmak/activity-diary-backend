package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.dictionary.DictionaryOptionDto;
import com.example.activity_diary.dto.mapper.DictionaryMapper;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.tag.TagMetricLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagMetricServiceImplTest {

    @Mock
    private TagMetricLinkRepository tagMetricLinkRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private DictionaryMapper dictionaryMapper;

    @InjectMocks
    private TagMetricServiceImpl service;

    @Test
    void getMetricsByTagId_returnsVisibleMetricOptions() {
        Tag tag = tag(7L, TagStatus.APPROVED);
        DictionaryItem metric = dictionaryItem(10L, "Distance");
        DictionaryOptionDto dto = new DictionaryOptionDto(10L, "Distance");

        when(tagRepository.findAllById(Set.of(7L))).thenReturn(List.of(tag));
        when(tagMetricLinkRepository.findVisibleMetricNamesByTagIds(Set.of(7L), Role.USER)).thenReturn(List.of(metric));
        when(dictionaryMapper.toOptionDto(metric)).thenReturn(dto);

        List<DictionaryOptionDto> result = service.getMetricsByTagId(7L, 5L, Role.USER);

        assertEquals(List.of(dto), result);
    }

    @Test
    void getMetricsByTagIds_returnsUnionMetricOptions() {
        Tag firstTag = tag(7L, TagStatus.APPROVED);
        Tag secondTag = tag(8L, TagStatus.APPROVED);
        DictionaryItem firstMetric = dictionaryItem(10L, "Distance");
        DictionaryItem secondMetric = dictionaryItem(20L, "Reps");
        DictionaryItem thirdMetric = dictionaryItem(30L, "Weight");
        DictionaryOptionDto firstDto = new DictionaryOptionDto(10L, "Distance");
        DictionaryOptionDto secondDto = new DictionaryOptionDto(20L, "Reps");
        DictionaryOptionDto thirdDto = new DictionaryOptionDto(30L, "Weight");

        when(tagRepository.findAllById(Set.of(7L, 8L))).thenReturn(List.of(firstTag, secondTag));
        when(tagMetricLinkRepository.findVisibleMetricNamesPageByTagIds(Set.of(7L, 8L), Role.USER, PageRequest.of(0, 6)))
                .thenReturn(new PageImpl<>(List.of(firstMetric, secondMetric, thirdMetric), PageRequest.of(0, 6), 3));
        when(dictionaryMapper.toOptionDto(firstMetric)).thenReturn(firstDto);
        when(dictionaryMapper.toOptionDto(secondMetric)).thenReturn(secondDto);
        when(dictionaryMapper.toOptionDto(thirdMetric)).thenReturn(thirdDto);

        var result = service.getMetricsByTagIds(List.of(7L, 8L), 5L, Role.USER, null, PageRequest.of(0, 6));

        assertEquals(List.of(firstDto, secondDto, thirdDto), result.items());
        assertEquals(3L, result.totalElements());
        verify(tagMetricLinkRepository).findVisibleMetricNamesPageByTagIds(Set.of(7L, 8L), Role.USER, PageRequest.of(0, 6));
    }

    @Test
    void getMetricsByTagId_hiddenTagForUser_throwsNotFound() {
        Tag tag = tag(7L, TagStatus.REJECTED);
        when(tagRepository.findAllById(Set.of(7L))).thenReturn(List.of(tag));

        assertThrows(NotFoundException.class, () -> service.getMetricsByTagId(7L, 5L, Role.USER));

        verify(tagMetricLinkRepository, never()).findVisibleMetricNamesPageByTagIds(Set.of(7L), Role.USER, PageRequest.of(0, 6));
    }

    @Test
    void validateMetricTypesAllowedForTags_whenMetricNotLinked_throwsBadRequest() {
        Tag tag = tag(7L, TagStatus.APPROVED);

        when(tagMetricLinkRepository.findMetricNameIdsByTagIds(Set.of(7L))).thenReturn(Set.of(10L));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.validateMetricTypesAllowedForTags(Set.of(tag), List.of(10L, 20L))
        );

        assertEquals("Metric 20 is not allowed for selected tag", exception.getMessage());
    }

    @Test
    void validateMetricTypesAllowedForTags_usesUnionAcrossAllTags() {
        Tag firstTag = tag(7L, TagStatus.APPROVED);
        Tag secondTag = tag(8L, TagStatus.APPROVED);

        when(tagMetricLinkRepository.findMetricNameIdsByTagIds(Set.of(7L, 8L))).thenReturn(Set.of(10L, 20L, 30L));

        service.validateMetricTypesAllowedForTags(Set.of(firstTag, secondTag), List.of(10L, 30L));

        verify(tagMetricLinkRepository).findMetricNameIdsByTagIds(Set.of(7L, 8L));
    }

    @Test
    void validateMetricTypesAllowedForTags_withoutTags_throwsBadRequest() {
        assertThrows(
                BadRequestException.class,
                () -> service.validateMetricTypesAllowedForTags(Set.of(), List.of(10L))
        );
    }

    private static Tag tag(Long id, TagStatus status) {
        Tag tag = Tag.builder()
                .name("tag")
                .status(status)
                .createdBy(user(5L))
                .build();
        tag.setId(id);
        return tag;
    }

    private static User user(Long id) {
        User user = User.builder().username("user").build();
        user.setId(id);
        return user;
    }

    private static DictionaryItem dictionaryItem(Long id, String label) {
        DictionaryItem item = DictionaryItem.builder()
                .type(DictionaryType.METRIC_NAME)
                .label(label)
                .active(true)
                .build();
        item.setId(id);
        return item;
    }
}
