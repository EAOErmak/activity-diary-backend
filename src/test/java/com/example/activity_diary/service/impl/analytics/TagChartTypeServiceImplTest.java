package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.tag.TagChartTypeLinkRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagChartTypeServiceImplTest {

    @Mock
    private TagChartTypeLinkRepository tagChartTypeLinkRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagChartTypeServiceImpl service;

    @Test
    void getChartTypesByTagId_returnsLinkedChartTypes() {
        Tag tag = tag(7L, "food");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(tagChartTypeLinkRepository.findChartTypesByTagId(7L)).thenReturn(List.of(
                ChartType.PFC_PER_EATING,
                ChartType.CALORIES_PER_EATING,
                ChartType.PFC_PER_DAY,
                ChartType.CALORIES_PER_DAY
        ));

        List<ChartType> result = service.getChartTypesByTagId(7L);

        assertEquals(
                List.of(
                        ChartType.CALORIES_PER_DAY,
                        ChartType.PFC_PER_DAY,
                        ChartType.CALORIES_PER_EATING,
                        ChartType.PFC_PER_EATING
                ),
                result
        );
    }

    @Test
    void getChartTypesByTagId_missingTag_throwsNotFound() {
        when(tagRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getChartTypesByTagId(7L));
    }

    @Test
    void validateChartTypeAllowed_whenLinkMissing_throwsBadRequest() {
        Tag tag = tag(7L, "Training");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(tagChartTypeLinkRepository.existsByTagIdAndChartType(7L, ChartType.TRAINING_RAW)).thenReturn(false);

        assertThrows(
                BadRequestException.class,
                () -> service.validateChartTypeAllowed(7L, ChartType.TRAINING_RAW)
        );
    }

    @Test
    void validateChartTypeAllowed_whenAllowed_passes() {
        Tag tag = tag(7L, "Training");

        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(tagChartTypeLinkRepository.existsByTagIdAndChartType(7L, ChartType.TRAINING_RAW)).thenReturn(true);

        service.validateChartTypeAllowed(7L, ChartType.TRAINING_RAW);

        verify(tagChartTypeLinkRepository).existsByTagIdAndChartType(7L, ChartType.TRAINING_RAW);
    }

    @Test
    void validateChartTypeAllowed_whenChartTypeMissing_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.validateChartTypeAllowed(7L, null));
        verify(tagRepository, never()).findById(7L);
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
