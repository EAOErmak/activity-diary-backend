package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateCreateDto;
import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateUpdateDto;
import com.example.activity_diary.dto.template.diary.DiaryEntryTemplateViewDto;
import com.example.activity_diary.dto.template.diary.EntryTemplateMetricUpsertDto;
import com.example.activity_diary.dto.template.diary.EntryTemplateMetricValueUpsertDto;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.template.DiaryEntryTemplate;
import com.example.activity_diary.entity.template.EntryTemplateMetric;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.template.DiaryEntryTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryEntryTemplateServiceImplTest {

    @Mock
    private DiaryEntryTemplateRepository diaryEntryTemplateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DictionaryRepository dictionaryRepository;

    @Spy
    private DiaryDescriptionTagPolicy diaryDescriptionTagPolicy = new DiaryDescriptionTagPolicy();

    @InjectMocks
    private DiaryEntryTemplateServiceImpl service;

    @Test
    void create_userMissing_throwsNotFound() {
        DiaryEntryTemplateCreateDto dto = validCreateDto("tpl", "desc");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_blankName_throwsBadRequest() {
        DiaryEntryTemplateCreateDto dto = validCreateDto("   ", "desc");
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));

        assertThrows(BadRequestException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_nameExists_throwsBadRequest() {
        DiaryEntryTemplateCreateDto dto = validCreateDto(" Name ", "desc");
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(diaryEntryTemplateRepository.existsByUser_IdAndNameIgnoreCase(1L, "Name"))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_invalidMood_throwsBadRequest() {
        DiaryEntryTemplateCreateDto dto = validCreateDto("tpl", "desc");
        dto.setMood((short) 6);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));

        assertThrows(BadRequestException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_blankDescription_throwsBadRequest() {
        DiaryEntryTemplateCreateDto dto = validCreateDto("tpl", "  ");
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));

        assertThrows(BadRequestException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_descriptionWithoutTags_throwsBadRequest() {
        DiaryEntryTemplateCreateDto dto = validCreateDto("tpl", "desc");
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(diaryEntryTemplateRepository.existsByUser_IdAndNameIgnoreCase(1L, "tpl")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_success_withMetrics_returnsView() {
        DiaryEntryTemplateCreateDto dto = validCreateDto("tpl", "#desc");
        dto.setMetrics(List.of(metric(10L, 20L, BigDecimal.valueOf(5))));

        User user = userWithId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(diaryEntryTemplateRepository.existsByUser_IdAndNameIgnoreCase(1L, "tpl")).thenReturn(false);

        DictionaryItem metricType = dictItem(10L, DictionaryType.METRIC_NAME, "m");
        DictionaryItem unit = dictItem(20L, DictionaryType.METRIC_UNIT, "u");
        when(dictionaryRepository.findAllById(Set.of(10L, 20L)))
                .thenReturn(List.of(metricType, unit));

        when(diaryEntryTemplateRepository.save(any(DiaryEntryTemplate.class)))
                .thenAnswer(invocation -> {
                    DiaryEntryTemplate t = invocation.getArgument(0);
                    t.setId(50L);
                    return t;
                });

        DiaryEntryTemplateViewDto result = service.create(1L, dto);

        assertEquals("tpl", result.getName());
        assertEquals(1, result.getMetrics().size());
        assertEquals(new BigDecimal("5.00000"), result.getMetrics().getFirst().getValues().getFirst().getValue());
    }

    @Test
    void create_sortsMetricsAndValuesByDictionaryItemIdBeforeSaving() {
        DiaryEntryTemplateCreateDto dto = validCreateDto("tpl", "#desc");
        dto.setMetrics(List.of(
                metric(20L, 200L, BigDecimal.valueOf(5), 100L, BigDecimal.valueOf(3)),
                metric(10L, 300L, BigDecimal.valueOf(7))
        ));

        User user = userWithId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(diaryEntryTemplateRepository.existsByUser_IdAndNameIgnoreCase(1L, "tpl")).thenReturn(false);
        when(dictionaryRepository.findAllById(Set.of(20L, 200L, 100L, 10L, 300L)))
                .thenReturn(List.of(
                        dictItem(20L, DictionaryType.METRIC_NAME, "m20"),
                        dictItem(200L, DictionaryType.METRIC_UNIT, "u200"),
                        dictItem(100L, DictionaryType.METRIC_UNIT, "u100"),
                        dictItem(10L, DictionaryType.METRIC_NAME, "m10"),
                        dictItem(300L, DictionaryType.METRIC_UNIT, "u300")
                ));
        when(diaryEntryTemplateRepository.save(any(DiaryEntryTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(1L, dto);

        ArgumentCaptor<DiaryEntryTemplate> templateCaptor = ArgumentCaptor.forClass(DiaryEntryTemplate.class);
        verify(diaryEntryTemplateRepository).save(templateCaptor.capture());

        DiaryEntryTemplate saved = templateCaptor.getValue();
        assertEquals(
                List.of(10L, 20L),
                saved.getMetrics().stream()
                        .map(metric -> metric.getMetricType().getId())
                        .toList()
        );
        assertEquals(
                List.of(100L, 200L),
                saved.getMetrics().get(1).getValues().stream()
                        .map(value -> value.getUnit().getId())
                        .toList()
        );
    }

    @Test
    void update_duplicateUnitId_throwsBadRequest() {
        DiaryEntryTemplate template = DiaryEntryTemplate.builder()
                .user(userWithId(1L))
                .name("tpl")
                .description("#desc")
                .build();
        template.setId(11L);

        when(diaryEntryTemplateRepository.findByIdAndUser_Id(11L, 1L))
                .thenReturn(Optional.of(template));

        DiaryEntryTemplateUpdateDto dto = new DiaryEntryTemplateUpdateDto();
        dto.setMetrics(List.of(metric(10L, 20L, BigDecimal.ONE, 20L, BigDecimal.valueOf(2))));

        DictionaryItem metricType = dictItem(10L, DictionaryType.METRIC_NAME, "m");
        DictionaryItem unit = dictItem(20L, DictionaryType.METRIC_UNIT, "u");
        when(dictionaryRepository.findAllById(Set.of(10L, 20L)))
                .thenReturn(List.of(metricType, unit));

        assertThrows(BadRequestException.class, () -> service.update(1L, 11L, dto));
    }

    @Test
    void get_sortsMetricsAndValuesInViewDto() {
        DiaryEntryTemplate template = DiaryEntryTemplate.create(
                userWithId(1L),
                "tpl",
                (short) 3,
                "#desc",
                null,
                null
        );
        template.setId(11L);

        EntryTemplateMetric secondMetric = EntryTemplateMetric.create(
                template,
                dictItem(20L, DictionaryType.METRIC_NAME, "m20")
        );
        secondMetric.addValue(dictItem(200L, DictionaryType.METRIC_UNIT, "u200"), BigDecimal.valueOf(5));
        secondMetric.addValue(dictItem(100L, DictionaryType.METRIC_UNIT, "u100"), BigDecimal.valueOf(3));
        template.addMetric(secondMetric);

        EntryTemplateMetric firstMetric = EntryTemplateMetric.create(
                template,
                dictItem(10L, DictionaryType.METRIC_NAME, "m10")
        );
        firstMetric.addValue(dictItem(300L, DictionaryType.METRIC_UNIT, "u300"), BigDecimal.valueOf(7));
        template.addMetric(firstMetric);

        when(diaryEntryTemplateRepository.findByIdAndUser_Id(11L, 1L))
                .thenReturn(Optional.of(template));

        DiaryEntryTemplateViewDto result = service.get(1L, 11L);

        assertEquals(
                List.of(10L, 20L),
                result.getMetrics().stream()
                        .map(metric -> metric.getMetricTypeId())
                        .toList()
        );
        assertEquals(
                List.of(100L, 200L),
                result.getMetrics().get(1).getValues().stream()
                        .map(value -> value.getUnitId())
                        .toList()
        );
    }

    @Test
    void list_returnsPage() {
        DiaryEntryTemplate template = DiaryEntryTemplate.builder()
                .user(userWithId(1L))
                .name("tpl")
                .description("#desc")
                .build();
        template.setId(5L);

        when(diaryEntryTemplateRepository.findAllByUser_Id(1L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(template)));

        assertEquals(1, service.list(1L, PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void delete_missing_throwsNotFound() {
        when(diaryEntryTemplateRepository.findByIdAndUser_Id(7L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.delete(1L, 7L));
    }

    @Test
    void update_descriptionWithoutTags_throwsBadRequest() {
        DiaryEntryTemplate template = DiaryEntryTemplate.builder()
                .user(userWithId(1L))
                .name("tpl")
                .description("#desc")
                .build();
        template.setId(11L);

        when(diaryEntryTemplateRepository.findByIdAndUser_Id(11L, 1L))
                .thenReturn(Optional.of(template));

        DiaryEntryTemplateUpdateDto dto = new DiaryEntryTemplateUpdateDto();
        dto.setDescription("plain text");

        assertThrows(BadRequestException.class, () -> service.update(1L, 11L, dto));
    }

    private static DiaryEntryTemplateCreateDto validCreateDto(String name, String desc) {
        DiaryEntryTemplateCreateDto dto = new DiaryEntryTemplateCreateDto();
        dto.setName(name);
        dto.setDescription(desc);
        dto.setMood((short) 3);
        return dto;
    }

    private static EntryTemplateMetricUpsertDto metric(Long metricTypeId, Long unitId, BigDecimal value) {
        EntryTemplateMetricValueUpsertDto v = new EntryTemplateMetricValueUpsertDto();
        v.setUnitId(unitId);
        v.setValue(value);

        EntryTemplateMetricUpsertDto m = new EntryTemplateMetricUpsertDto();
        m.setMetricTypeId(metricTypeId);
        m.setValues(List.of(v));
        return m;
    }

    private static EntryTemplateMetricUpsertDto metric(
            Long metricTypeId,
            Long unitId1,
            BigDecimal value1,
            Long unitId2,
            BigDecimal value2
    ) {
        EntryTemplateMetricValueUpsertDto v1 = new EntryTemplateMetricValueUpsertDto();
        v1.setUnitId(unitId1);
        v1.setValue(value1);

        EntryTemplateMetricValueUpsertDto v2 = new EntryTemplateMetricValueUpsertDto();
        v2.setUnitId(unitId2);
        v2.setValue(value2);

        EntryTemplateMetricUpsertDto m = new EntryTemplateMetricUpsertDto();
        m.setMetricTypeId(metricTypeId);
        m.setValues(List.of(v1, v2));
        return m;
    }

    private static User userWithId(Long id) {
        User user = User.builder().username("user").build();
        user.setId(id);
        return user;
    }

    private static DictionaryItem dictItem(Long id, DictionaryType type, String label) {
        DictionaryItem item = DictionaryItem.builder()
                .type(type)
                .label(label)
                .build();
        item.setId(id);
        return item;
    }
}
