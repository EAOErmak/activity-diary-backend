package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.admin.TagChartTypeLinkResponseDto;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.diary.TagChartTypeLink;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.entity.enums.GlobalSyncEntityType;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.tag.TagChartTypeLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.service.sync.GlobalSyncService;
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
class AdminTagChartTypeServiceImplTest {

    @Mock
    private TagChartTypeLinkRepository tagChartTypeLinkRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private GlobalSyncService globalSyncService;

    @InjectMocks
    private AdminTagChartTypeServiceImpl service;

    @Test
    void createLink_savesLinkAndBumpsSync() {
        Tag tag = tag(7L, "training");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(tagChartTypeLinkRepository.existsByTagIdAndChartType(7L, ChartType.TRAINING_RAW)).thenReturn(false);
        when(tagChartTypeLinkRepository.saveAndFlush(any(TagChartTypeLink.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TagChartTypeLinkResponseDto result = service.createLink(7L, ChartType.TRAINING_RAW);

        assertEquals(7L, result.getTagId());
        assertEquals(ChartType.TRAINING_RAW, result.getChartType());

        ArgumentCaptor<TagChartTypeLink> captor = ArgumentCaptor.forClass(TagChartTypeLink.class);
        verify(tagChartTypeLinkRepository).saveAndFlush(captor.capture());
        assertEquals(7L, captor.getValue().getTag().getId());
        assertEquals(ChartType.TRAINING_RAW, captor.getValue().getChartType());
        verify(globalSyncService).bump(GlobalSyncEntityType.TAG);
    }

    @Test
    void createLink_duplicate_throwsBadRequest() {
        Tag tag = tag(7L, "training");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(tagChartTypeLinkRepository.existsByTagIdAndChartType(7L, ChartType.TRAINING_RAW)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.createLink(7L, ChartType.TRAINING_RAW));
        verify(tagChartTypeLinkRepository, never()).saveAndFlush(any(TagChartTypeLink.class));
    }

    @Test
    void createLink_uniqueConstraintRace_throwsBadRequest() {
        Tag tag = tag(7L, "training");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(tagChartTypeLinkRepository.existsByTagIdAndChartType(7L, ChartType.TRAINING_RAW)).thenReturn(false);
        when(tagChartTypeLinkRepository.saveAndFlush(any(TagChartTypeLink.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(BadRequestException.class, () -> service.createLink(7L, ChartType.TRAINING_RAW));
        verify(globalSyncService, never()).bump(GlobalSyncEntityType.TAG);
    }

    @Test
    void createLink_missingTag_throwsNotFound() {
        when(tagRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.createLink(7L, ChartType.TRAINING_RAW));
        verify(tagChartTypeLinkRepository, never()).existsByTagIdAndChartType(7L, ChartType.TRAINING_RAW);
    }

    @Test
    void deleteLink_existing_deletesAndBumpsSync() {
        Tag tag = tag(7L, "training");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(tagChartTypeLinkRepository.existsByTagIdAndChartType(7L, ChartType.TRAINING_RAW)).thenReturn(true);

        service.deleteLink(7L, ChartType.TRAINING_RAW);

        verify(tagChartTypeLinkRepository).deleteByTagIdAndChartType(7L, ChartType.TRAINING_RAW);
        verify(globalSyncService).bump(GlobalSyncEntityType.TAG);
    }

    @Test
    void deleteLink_missingLink_throwsBadRequest() {
        Tag tag = tag(7L, "training");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(tagChartTypeLinkRepository.existsByTagIdAndChartType(7L, ChartType.TRAINING_RAW)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> service.deleteLink(7L, ChartType.TRAINING_RAW));
        verify(tagChartTypeLinkRepository, never()).deleteByTagIdAndChartType(7L, ChartType.TRAINING_RAW);
    }

    @Test
    void getChartTypesByTagId_mapsSortedDtos() {
        Tag tag = tag(7L, "food");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(tagChartTypeLinkRepository.findChartTypesByTagId(7L)).thenReturn(List.of(
                ChartType.PFC_PER_EATING,
                ChartType.CALORIES_PER_EATING,
                ChartType.PFC_PER_DAY,
                ChartType.CALORIES_PER_DAY
        ));

        List<TagChartTypeLinkResponseDto> result = service.getChartTypesByTagId(7L);

        assertEquals(
                List.of(
                        ChartType.CALORIES_PER_DAY,
                        ChartType.PFC_PER_DAY,
                        ChartType.CALORIES_PER_EATING,
                        ChartType.PFC_PER_EATING
                ),
                result.stream().map(TagChartTypeLinkResponseDto::getChartType).toList()
        );
        assertEquals(List.of(7L, 7L, 7L, 7L), result.stream().map(TagChartTypeLinkResponseDto::getTagId).toList());
    }

    private static Tag tag(Long id, String name) {
        Tag tag = Tag.builder()
                .name(name)
                .status(TagStatus.APPROVED)
                .build();
        tag.setId(id);
        return tag;
    }
}
