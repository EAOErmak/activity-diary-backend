package com.example.activity_diary.service.impl.dictionary;

import com.example.activity_diary.dto.dictionary.DictionaryCreateDto;
import com.example.activity_diary.dto.dictionary.DictionaryOptionDto;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.dto.mapper.DictionaryMapper;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.Role;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.diary.MetricNameUnitLinkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceImplTest {

    @Mock
    private DictionaryRepository dictionaryRepository;

    @Mock
    private MetricNameUnitLinkRepository metricNameUnitLinkRepository;

    @Mock
    private DictionaryMapper mapper;

    @InjectMocks
    private DictionaryServiceImpl service;

    @Test
    void create_savesDictionaryItemWithoutLegacyField() {
        DictionaryCreateDto dto = new DictionaryCreateDto();
        dto.setType(DictionaryType.METRIC_NAME);
        dto.setLabel(" Protein ");
        dto.setAllowedRole(Role.PREMIUM);

        DictionaryItem saved = dictionaryItem(10L, DictionaryType.METRIC_NAME, "Protein");
        saved.setAllowedRole(Role.PREMIUM);

        DictionaryResponseDto response = new DictionaryResponseDto();
        response.setId(10L);
        response.setType(DictionaryType.METRIC_NAME);
        response.setLabel("Protein");
        response.setAllowedRole("PREMIUM");
        response.setActive(true);

        when(dictionaryRepository.existsByTypeAndLabelIgnoreCase(DictionaryType.METRIC_NAME, "Protein"))
                .thenReturn(false);
        when(dictionaryRepository.save(any(DictionaryItem.class))).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(response);

        DictionaryResponseDto result = service.create(dto);

        assertEquals(response, result);

        ArgumentCaptor<DictionaryItem> captor = ArgumentCaptor.forClass(DictionaryItem.class);
        verify(dictionaryRepository).save(captor.capture());
        assertEquals(DictionaryType.METRIC_NAME, captor.getValue().getType());
        assertEquals("Protein", captor.getValue().getLabel());
        assertEquals(Role.PREMIUM, captor.getValue().getAllowedRole());
        assertEquals(true, captor.getValue().isActive());
    }

    @Test
    void getUnitsByMetricNameId_returnsVisibleLinkedUnits() {
        DictionaryItem metricName = dictionaryItem(10L, DictionaryType.METRIC_NAME, "Weight");
        DictionaryItem visibleUnit = dictionaryItem(20L, DictionaryType.METRIC_UNIT, "kg");
        DictionaryItem hiddenUnit = dictionaryItem(21L, DictionaryType.METRIC_UNIT, "lb");
        hiddenUnit.setAllowedRole(Role.PREMIUM);

        DictionaryOptionDto visibleDto = new DictionaryOptionDto(20L, "kg");

        when(dictionaryRepository.findById(10L)).thenReturn(Optional.of(metricName));
        when(metricNameUnitLinkRepository.findUnitsByMetricNameId(10L)).thenReturn(List.of(visibleUnit, hiddenUnit));
        when(mapper.toOptionDto(visibleUnit)).thenReturn(visibleDto);

        List<DictionaryOptionDto> result = service.getUnitsByMetricNameId(10L, Role.USER);

        assertEquals(List.of(visibleDto), result);
    }

    @Test
    void getUnitsByMetricNameId_missingMetricName_throwsNotFound() {
        when(dictionaryRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getUnitsByMetricNameId(10L, Role.USER));
        verifyNoInteractions(metricNameUnitLinkRepository, mapper);
    }

    @Test
    void getUnitsByMetricNameId_nonMetricName_throwsBadRequest() {
        DictionaryItem metricUnit = dictionaryItem(10L, DictionaryType.METRIC_UNIT, "kg");
        when(dictionaryRepository.findById(10L)).thenReturn(Optional.of(metricUnit));

        assertThrows(BadRequestException.class, () -> service.getUnitsByMetricNameId(10L, Role.USER));
        verifyNoInteractions(metricNameUnitLinkRepository, mapper);
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
