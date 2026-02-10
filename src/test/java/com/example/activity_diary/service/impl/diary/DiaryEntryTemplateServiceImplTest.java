package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.template.DiaryEntryTemplateCreateDto;
import com.example.activity_diary.dto.template.DiaryEntryTemplateUpdateDto;
import com.example.activity_diary.dto.template.DiaryEntryTemplateViewDto;
import com.example.activity_diary.dto.template.EntryTemplateMetricUpsertDto;
import com.example.activity_diary.dto.template.EntryTemplateMetricValueUpsertDto;
import com.example.activity_diary.entity.DiaryEntryTemplate;
import com.example.activity_diary.entity.Tag;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.repository.template.DiaryEntryTemplateRepository;
import com.example.activity_diary.service.diary.TagResolverService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryEntryTemplateServiceImplTest {

    @Mock
    private DiaryEntryTemplateRepository diaryEntryTemplateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private DictionaryRepository dictionaryRepository;

    @Mock
    private TagResolverService tagResolverService;

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
        verify(tagResolverService, never()).resolveFromDescription(any(), any());
    }

    @Test
    void create_noTags_throwsBadRequest() {
        DiaryEntryTemplateCreateDto dto = validCreateDto("tpl", "desc");
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(tagResolverService.resolveFromDescription(1L, "desc")).thenReturn(Set.of());

        assertThrows(BadRequestException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_success_withMetrics_returnsView() {
        DiaryEntryTemplateCreateDto dto = validCreateDto("tpl", "desc");
        dto.setMetrics(List.of(metric(10L, 20L, 5)));

        User user = userWithId(1L);
        Tag tag = Tag.builder().name("t").build();
        tag.setId(100L);
        Set<Tag> tags = Set.of(tag);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(diaryEntryTemplateRepository.existsByUser_IdAndNameIgnoreCase(1L, "tpl")).thenReturn(false);
        when(tagResolverService.resolveFromDescription(1L, "desc")).thenReturn(tags);

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
        assertEquals(1, result.getTags().size());
        assertEquals(1, result.getMetrics().size());
    }

    @Test
    void update_duplicateUnitId_throwsBadRequest() {
        DiaryEntryTemplate template = DiaryEntryTemplate.builder()
                .user(userWithId(1L))
                .name("tpl")
                .description("desc")
                .build();
        template.setId(11L);

        when(diaryEntryTemplateRepository.findByIdAndUser_Id(11L, 1L))
                .thenReturn(Optional.of(template));

        DiaryEntryTemplateUpdateDto dto = new DiaryEntryTemplateUpdateDto();
        dto.setMetrics(List.of(metric(10L, 20L, 1, 20L, 2)));

        DictionaryItem metricType = dictItem(10L, DictionaryType.METRIC_NAME, "m");
        DictionaryItem unit = dictItem(20L, DictionaryType.METRIC_UNIT, "u");
        when(dictionaryRepository.findAllById(Set.of(10L, 20L)))
                .thenReturn(List.of(metricType, unit));

        assertThrows(BadRequestException.class, () -> service.update(1L, 11L, dto));
    }

    @Test
    void list_returnsPage() {
        DiaryEntryTemplate template = DiaryEntryTemplate.builder()
                .user(userWithId(1L))
                .name("tpl")
                .description("desc")
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

    private static DiaryEntryTemplateCreateDto validCreateDto(String name, String desc) {
        DiaryEntryTemplateCreateDto dto = new DiaryEntryTemplateCreateDto();
        dto.setName(name);
        dto.setDescription(desc);
        dto.setMood((short) 3);
        return dto;
    }

    private static EntryTemplateMetricUpsertDto metric(Long metricTypeId, Long unitId, Integer value) {
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
            Integer value1,
            Long unitId2,
            Integer value2
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
