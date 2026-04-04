package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.admin.MetricLinkResponseDto;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.dict.MetricNameUnitLink;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.GlobalSyncEntityType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.diary.MetricNameUnitLinkRepository;
import com.example.activity_diary.service.sync.GlobalSyncService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMetricLinkServiceImplTest {

    @Mock
    private MetricNameUnitLinkRepository metricNameUnitLinkRepository;

    @Mock
    private DictionaryRepository dictionaryRepository;

    @Mock
    private GlobalSyncService globalSyncService;

    @InjectMocks
    private AdminMetricLinkServiceImpl service;

    @Test
    void createLink_savesMetricLink() {
        DictionaryItem metricName = dictionaryItem(10L, DictionaryType.METRIC_NAME, "Weight");
        DictionaryItem metricUnit = dictionaryItem(20L, DictionaryType.METRIC_UNIT, "kg");

        when(dictionaryRepository.findById(10L)).thenReturn(Optional.of(metricName));
        when(dictionaryRepository.findById(20L)).thenReturn(Optional.of(metricUnit));
        when(metricNameUnitLinkRepository.existsByMetricNameIdAndMetricUnitId(10L, 20L)).thenReturn(false);
        when(metricNameUnitLinkRepository.save(org.mockito.ArgumentMatchers.any(MetricNameUnitLink.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MetricLinkResponseDto result = service.createLink(10L, 20L);

        assertEquals(20L, result.getId());
        assertEquals("kg", result.getLabel());

        ArgumentCaptor<MetricNameUnitLink> captor = ArgumentCaptor.forClass(MetricNameUnitLink.class);
        verify(metricNameUnitLinkRepository).save(captor.capture());
        assertEquals(10L, captor.getValue().getMetricName().getId());
        assertEquals(20L, captor.getValue().getMetricUnit().getId());
        verify(globalSyncService).bump(GlobalSyncEntityType.DICTIONARY);
    }

    @Test
    void createLink_duplicate_throwsBadRequest() {
        DictionaryItem metricName = dictionaryItem(10L, DictionaryType.METRIC_NAME, "Weight");
        DictionaryItem metricUnit = dictionaryItem(20L, DictionaryType.METRIC_UNIT, "kg");

        when(dictionaryRepository.findById(10L)).thenReturn(Optional.of(metricName));
        when(dictionaryRepository.findById(20L)).thenReturn(Optional.of(metricUnit));
        when(metricNameUnitLinkRepository.existsByMetricNameIdAndMetricUnitId(10L, 20L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.createLink(10L, 20L));
        verify(metricNameUnitLinkRepository, never()).save(org.mockito.ArgumentMatchers.any(MetricNameUnitLink.class));
    }

    @Test
    void createLink_wrongDictionaryType_throwsBadRequest() {
        DictionaryItem metricName = dictionaryItem(10L, DictionaryType.METRIC_UNIT, "kg");
        when(dictionaryRepository.findById(10L)).thenReturn(Optional.of(metricName));

        assertThrows(BadRequestException.class, () -> service.createLink(10L, 20L));
        verify(metricNameUnitLinkRepository, never()).existsByMetricNameIdAndMetricUnitId(10L, 20L);
    }

    @Test
    void deleteLink_existing_deletesAndBumpsSync() {
        DictionaryItem metricName = dictionaryItem(10L, DictionaryType.METRIC_NAME, "Weight");
        DictionaryItem metricUnit = dictionaryItem(20L, DictionaryType.METRIC_UNIT, "kg");

        when(dictionaryRepository.findById(10L)).thenReturn(Optional.of(metricName));
        when(dictionaryRepository.findById(20L)).thenReturn(Optional.of(metricUnit));
        when(metricNameUnitLinkRepository.existsByMetricNameIdAndMetricUnitId(10L, 20L)).thenReturn(true);

        service.deleteLink(10L, 20L);

        verify(metricNameUnitLinkRepository).deleteByMetricNameIdAndMetricUnitId(10L, 20L);
        verify(globalSyncService).bump(GlobalSyncEntityType.DICTIONARY);
    }

    @Test
    void getUnitsByMetricName_mapsUnits() {
        DictionaryItem metricName = dictionaryItem(10L, DictionaryType.METRIC_NAME, "Weight");
        DictionaryItem kilogram = dictionaryItem(20L, DictionaryType.METRIC_UNIT, "kg");
        DictionaryItem pound = dictionaryItem(30L, DictionaryType.METRIC_UNIT, "lb");

        when(dictionaryRepository.findById(10L)).thenReturn(Optional.of(metricName));
        when(metricNameUnitLinkRepository.findByMetricNameId(10L)).thenReturn(List.of(
                MetricNameUnitLink.create(metricName, kilogram),
                MetricNameUnitLink.create(metricName, pound)
        ));

        List<MetricLinkResponseDto> result = service.getUnitsByMetricName(10L);

        assertEquals(2, result.size());
        assertEquals(List.of("kg", "lb"), result.stream().map(MetricLinkResponseDto::getLabel).toList());
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
