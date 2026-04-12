package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.diary.DiaryEntryViewDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricCreateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricUpdateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueCreateDto;
import com.example.activity_diary.dto.diary.metric.EntryMetricValueUpdateDto;
import com.example.activity_diary.dto.mapper.DiaryEntryMapper;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.example.activity_diary.entity.enums.UiStatus;
import com.example.activity_diary.entity.enums.UserSyncEntityType;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.diary.DiaryRepository;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.service.analytics.MetricUsageAggService;
import com.example.activity_diary.service.analytics.TagUsageAggService;
import com.example.activity_diary.service.diary.DiaryValidationService;
import com.example.activity_diary.service.diary.TagResolverService;
import com.example.activity_diary.service.sync.UserSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryServiceImplTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DictionaryRepository dictionaryRepository;

    @Mock
    private DiaryValidationService validationService;

    @Mock
    private UserSyncService userSyncService;

    @Mock
    private TagResolverService tagResolverService;

    @Mock
    private DiaryEntryMapper mapper;

    @Mock
    private TagUsageAggService tagUsageAggService;

    @Mock
    private MetricUsageAggService metricUsageAggService;

    @InjectMocks
    private DiaryServiceImpl service;

    @Test
    void getEntriesByDateRange_invalidRange_throwsBadRequest() {
        assertThrows(BadRequestException.class, () ->
                service.getEntriesByDateRange(1L, null, LocalDateTime.now()));
    }

    @Test
    void getMyEntriesFiltered_fromAfterTo_throwsBadRequest() {
        Instant from = Instant.parse("2026-02-10T12:00:00Z");
        Instant to = Instant.parse("2026-02-10T11:00:00Z");

        assertThrows(BadRequestException.class, () ->
                service.getMyEntriesFiltered(
                        1L,
                        UiStatus.ACTIVE,
                        Instant.parse("2026-02-10T10:00:00Z"),
                        List.of("tag"),
                        from,
                        to,
                        PageRequest.of(0, 10)
                ));
    }

    @Test
    void getMyEntriesFiltered_normalizesTags() {
        Slice<DiaryEntryViewDto> emptySlice = new SliceImpl<>(List.of());
        when(diaryRepository.findListByUserIdFilteredAndTags(
                anyLong(), any(), any(), anyBoolean(), anyList(), anyInt(), any(), any(), any()))
                .thenReturn(emptySlice);

        Instant now = Instant.parse("2026-02-10T10:00:00Z");
        service.getMyEntriesFiltered(
                7L,
                UiStatus.ACTIVE,
                now,
                java.util.Arrays.asList("  Foo ", "", null, "BAR", "foo"),
                null,
                null,
                PageRequest.of(0, 10)
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> tagsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Boolean> hasTagsCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Integer> tagCountCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(diaryRepository).findListByUserIdFilteredAndTags(
                eq(7L),
                eq(UiStatus.ACTIVE.name()),
                eq(now),
                hasTagsCaptor.capture(),
                tagsCaptor.capture(),
                tagCountCaptor.capture(),
                isNull(),
                isNull(),
                any()
        );

        List<String> normalized = tagsCaptor.getValue();
        assertEquals(List.of("foo", "bar"), normalized);
        assertTrue(hasTagsCaptor.getValue());
        assertEquals(2, tagCountCaptor.getValue());
    }

    @Test
    void getMyEntryById_missing_throwsNotFound() {
        when(diaryRepository.findGraphByIdAndUser_Id(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getMyEntryById(1L, 10L));
    }

    @Test
    void create_userMissing_throwsBadRequest() {
        DiaryEntryCreateDto dto = validCreateDto("desc");
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> service.create(dto, 10L));
        verify(validationService).validateCreate(dto);
    }

    @Test
    void create_blankDescription_throwsBadRequest() {
        DiaryEntryCreateDto dto = validCreateDto("   ");
        User user = userWithId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> service.create(dto, 10L));
        verify(tagResolverService, never()).resolveFromDescription(any(), any());
    }

    @Test
    void create_noTags_throwsBadRequest() {
        DiaryEntryCreateDto dto = validCreateDto("hello");
        User user = userWithId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(tagResolverService.resolveFromDescription(10L, "hello")).thenReturn(Set.of());

        assertThrows(BadRequestException.class, () -> service.create(dto, 10L));
    }

    @Test
    void create_success_savesAndBumps() {
        DiaryEntryCreateDto dto = validCreateDto("  hello ");
        User user = userWithId(10L);
        Tag tag = Tag.builder().name("tag").build();
        Set<Tag> tags = Set.of(tag);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(tagResolverService.resolveFromDescription(10L, "hello")).thenReturn(tags);

        when(diaryRepository.save(any(DiaryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DiaryEntryDto mapped = new DiaryEntryDto();
        when(mapper.toDto(any(DiaryEntry.class))).thenReturn(mapped);

        DiaryEntryDto result = service.create(dto, 10L);

        assertEquals(mapped, result);
        verify(metricUsageAggService).onEntryCreated(any(DiaryEntry.class));
        verify(tagUsageAggService).onEntryCreated(any(DiaryEntry.class));
        verify(userSyncService).bump(10L, UserSyncEntityType.DIARY);
    }

    @Test
    void create_sortsMetricsByMetricTypeIdBeforeSaving() {
        DiaryEntryCreateDto dto = validCreateDto("hello");
        dto.setMetrics(List.of(
                metricCreate(20L, 200L, 5),
                metricCreate(10L, 100L, 3)
        ));

        User user = userWithId(10L);
        Tag tag = Tag.builder().name("tag").build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(tagResolverService.resolveFromDescription(10L, "hello")).thenReturn(Set.of(tag));
        when(dictionaryRepository.findAllById(Set.of(20L, 10L, 200L, 100L))).thenReturn(List.of(
                dictionaryItem(20L, DictionaryType.METRIC_NAME),
                dictionaryItem(10L, DictionaryType.METRIC_NAME),
                dictionaryItem(200L, DictionaryType.METRIC_UNIT),
                dictionaryItem(100L, DictionaryType.METRIC_UNIT)
        ));
        when(diaryRepository.save(any(DiaryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(DiaryEntry.class))).thenReturn(new DiaryEntryDto());

        service.create(dto, 10L);

        ArgumentCaptor<DiaryEntry> entryCaptor = ArgumentCaptor.forClass(DiaryEntry.class);
        verify(diaryRepository).save(entryCaptor.capture());

        List<Long> metricTypeIds = entryCaptor.getValue().getMetrics().stream()
                .map(metric -> metric.getMetricType().getId())
                .toList();

        assertEquals(List.of(10L, 20L), metricTypeIds);
        verify(dictionaryRepository).findAllById(Set.of(20L, 10L, 200L, 100L));
        verify(dictionaryRepository, never()).findById(anyLong());
    }

    @Test
    void create_sortsMetricValuesByUnitIdBeforeSaving() {
        DiaryEntryCreateDto dto = validCreateDto("hello");

        EntryMetricValueCreateDto firstValue = new EntryMetricValueCreateDto();
        firstValue.setUnitId(200L);
        firstValue.setValue(5);

        EntryMetricValueCreateDto secondValue = new EntryMetricValueCreateDto();
        secondValue.setUnitId(100L);
        secondValue.setValue(3);

        EntryMetricCreateDto metricDto = new EntryMetricCreateDto();
        metricDto.setMetricTypeId(10L);
        metricDto.setValues(List.of(firstValue, secondValue));
        dto.setMetrics(List.of(metricDto));

        User user = userWithId(10L);
        Tag tag = Tag.builder().name("tag").build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(tagResolverService.resolveFromDescription(10L, "hello")).thenReturn(Set.of(tag));
        when(dictionaryRepository.findAllById(Set.of(10L, 200L, 100L))).thenReturn(List.of(
                dictionaryItem(10L, DictionaryType.METRIC_NAME),
                dictionaryItem(200L, DictionaryType.METRIC_UNIT),
                dictionaryItem(100L, DictionaryType.METRIC_UNIT)
        ));
        when(diaryRepository.save(any(DiaryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(DiaryEntry.class))).thenReturn(new DiaryEntryDto());

        service.create(dto, 10L);

        ArgumentCaptor<DiaryEntry> entryCaptor = ArgumentCaptor.forClass(DiaryEntry.class);
        verify(diaryRepository).save(entryCaptor.capture());

        List<Long> unitIds = entryCaptor.getValue().getMetrics().getFirst().getValues().stream()
                .map(value -> value.getUnit().getId())
                .toList();

        assertEquals(List.of(100L, 200L), unitIds);
        verify(dictionaryRepository).findAllById(Set.of(10L, 200L, 100L));
        verify(dictionaryRepository, never()).findById(anyLong());
    }

    @Test
    void update_metrics_prefetchesDictionaryInSingleBatch() {
        DiaryEntryUpdateDto dto = new DiaryEntryUpdateDto();
        dto.setMetrics(List.of(
                metricUpdate(20L, 200L, 5),
                metricUpdate(10L, 100L, 3)
        ));

        User user = userWithId(10L);
        DiaryEntry entry = entryForUser(
                user,
                Instant.now().minusSeconds(1200),
                Instant.now().minusSeconds(600)
        );

        when(diaryRepository.findGraphByIdAndUser_Id(1L, 10L)).thenReturn(Optional.of(entry));
        when(dictionaryRepository.findAllById(Set.of(20L, 10L, 200L, 100L))).thenReturn(List.of(
                dictionaryItem(20L, DictionaryType.METRIC_NAME),
                dictionaryItem(10L, DictionaryType.METRIC_NAME),
                dictionaryItem(200L, DictionaryType.METRIC_UNIT),
                dictionaryItem(100L, DictionaryType.METRIC_UNIT)
        ));
        when(diaryRepository.save(entry)).thenReturn(entry);
        when(mapper.toDto(entry)).thenReturn(new DiaryEntryDto());

        service.update(1L, dto, 10L);

        verify(dictionaryRepository).findAllById(Set.of(20L, 10L, 200L, 100L));
        verify(dictionaryRepository, never()).findById(anyLong());
    }

    @Test
    void create_futureEntry_setsScheduledStatus() {
        DiaryEntryCreateDto dto = validCreateDto(
                "hello",
                Instant.now().plusSeconds(600),
                Instant.now().plusSeconds(1200)
        );
        User user = userWithId(10L);
        Tag tag = Tag.builder().name("tag").build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(tagResolverService.resolveFromDescription(10L, "hello")).thenReturn(Set.of(tag));
        when(diaryRepository.save(any(DiaryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(DiaryEntry.class))).thenReturn(new DiaryEntryDto());

        service.create(dto, 10L);

        ArgumentCaptor<DiaryEntry> entryCaptor = ArgumentCaptor.forClass(DiaryEntry.class);
        verify(diaryRepository).save(entryCaptor.capture());
        assertEquals(EntryStatus.PLANNED, entryCaptor.getValue().getStatus());
    }

    @Test
    void create_currentEntry_setsActiveStatus() {
        DiaryEntryCreateDto dto = validCreateDto(
                "hello",
                Instant.now().minusSeconds(300),
                Instant.now().plusSeconds(300)
        );
        User user = userWithId(10L);
        Tag tag = Tag.builder().name("tag").build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(tagResolverService.resolveFromDescription(10L, "hello")).thenReturn(Set.of(tag));
        when(diaryRepository.save(any(DiaryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(DiaryEntry.class))).thenReturn(new DiaryEntryDto());

        service.create(dto, 10L);

        ArgumentCaptor<DiaryEntry> entryCaptor = ArgumentCaptor.forClass(DiaryEntry.class);
        verify(diaryRepository).save(entryCaptor.capture());
        assertEquals(EntryStatus.ACTIVE, entryCaptor.getValue().getStatus());
    }

    @Test
    void create_pastEntry_setsFinishedStatus() {
        DiaryEntryCreateDto dto = validCreateDto(
                "hello",
                Instant.now().minusSeconds(1200),
                Instant.now().minusSeconds(600)
        );
        User user = userWithId(10L);
        Tag tag = Tag.builder().name("tag").build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(tagResolverService.resolveFromDescription(10L, "hello")).thenReturn(Set.of(tag));
        when(diaryRepository.save(any(DiaryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(DiaryEntry.class))).thenReturn(new DiaryEntryDto());

        service.create(dto, 10L);

        ArgumentCaptor<DiaryEntry> entryCaptor = ArgumentCaptor.forClass(DiaryEntry.class);
        verify(diaryRepository).save(entryCaptor.capture());
        assertEquals(EntryStatus.FINISHED, entryCaptor.getValue().getStatus());
    }

    @Test
    void update_pastEntry_allowsTimeChange() {
        DiaryEntryUpdateDto dto = new DiaryEntryUpdateDto();
        dto.setWhenStarted(Instant.now().minusSeconds(900));
        dto.setWhenEnded(Instant.now().minusSeconds(300));

        User user = userWithId(10L);
        DiaryEntry entry = entryForUser(user,
                Instant.now().minusSeconds(1200),
                Instant.now().minusSeconds(600));

        when(diaryRepository.findGraphByIdAndUser_Id(1L, 10L)).thenReturn(Optional.of(entry));
        when(diaryRepository.save(entry)).thenReturn(entry);
        DiaryEntryDto mapped = new DiaryEntryDto();
        when(mapper.toDto(entry)).thenReturn(mapped);

        DiaryEntryDto result = service.update(1L, dto, 10L);

        assertEquals(mapped, result);
        assertEquals(dto.getWhenStarted(), entry.getWhenStarted());
        assertEquals(dto.getWhenEnded(), entry.getWhenEnded());
        verify(userSyncService).bump(10L, UserSyncEntityType.DIARY);
    }

    @Test
    void update_pastEntry_allowsStatusChange() {
        DiaryEntryUpdateDto dto = new DiaryEntryUpdateDto();
        dto.setStatus(EntryStatus.FINISHED);

        User user = userWithId(10L);
        DiaryEntry entry = entryForUser(user,
                Instant.now().minusSeconds(600),
                Instant.now().minusSeconds(300));

        when(diaryRepository.findGraphByIdAndUser_Id(1L, 10L)).thenReturn(Optional.of(entry));
        when(diaryRepository.save(entry)).thenReturn(entry);
        DiaryEntryDto mapped = new DiaryEntryDto();
        when(mapper.toDto(entry)).thenReturn(mapped);

        DiaryEntryDto result = service.update(1L, dto, 10L);

        assertEquals(mapped, result);
        assertEquals(EntryStatus.FINISHED, entry.getStatus());
        verify(userSyncService).bump(10L, UserSyncEntityType.DIARY);
    }

    @Test
    void update_description_resolvesTagsAndBumps() {
        DiaryEntryUpdateDto dto = new DiaryEntryUpdateDto();
        dto.setDescription("  new desc ");

        User user = userWithId(10L);
        DiaryEntry entry = entryForUser(user,
                Instant.now().plusSeconds(300),
                Instant.now().plusSeconds(600));

        Tag tag = Tag.builder().name("tag").build();
        when(diaryRepository.findGraphByIdAndUser_Id(1L, 10L)).thenReturn(Optional.of(entry));
        when(tagResolverService.resolveFromDescription(10L, "new desc")).thenReturn(Set.of(tag));
        when(diaryRepository.save(entry)).thenReturn(entry);
        DiaryEntryDto mapped = new DiaryEntryDto();
        when(mapper.toDto(entry)).thenReturn(mapped);

        DiaryEntryDto result = service.update(1L, dto, 10L);

        assertEquals(mapped, result);
        verify(metricUsageAggService).onEntryDeleted(entry);
        verify(tagUsageAggService).onEntryDeleted(entry);
        verify(metricUsageAggService).onEntryCreated(entry);
        verify(tagUsageAggService).onEntryCreated(entry);
        verify(userSyncService).bump(10L, UserSyncEntityType.DIARY);
    }

    @Test
    void update_descriptionWithoutTags_throwsBadRequest() {
        DiaryEntryUpdateDto dto = new DiaryEntryUpdateDto();
        dto.setDescription("new desc");

        User user = userWithId(10L);
        DiaryEntry entry = entryForUser(user,
                Instant.now().plusSeconds(300),
                Instant.now().plusSeconds(600));

        when(diaryRepository.findGraphByIdAndUser_Id(1L, 10L)).thenReturn(Optional.of(entry));
        when(tagResolverService.resolveFromDescription(10L, "new desc")).thenReturn(Set.of());

        assertThrows(BadRequestException.class, () -> service.update(1L, dto, 10L));
        verify(diaryRepository, never()).save(any(DiaryEntry.class));
    }

    @Test
    void delete_marksDeletedAdjustsAggregatesAndBumps() {
        User user = userWithId(10L);
        DiaryEntry entry = entryForUser(user,
                Instant.now().minusSeconds(1200),
                Instant.now().minusSeconds(600));

        when(diaryRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(entry));
        when(diaryRepository.save(entry)).thenReturn(entry);

        service.delete(1L, 10L);

        assertEquals(EntryStatus.DELETED, entry.getStatus());
        verify(metricUsageAggService).onEntryDeleted(entry);
        verify(tagUsageAggService).onEntryDeleted(entry);
        verify(userSyncService).bump(10L, UserSyncEntityType.DIARY);
    }

    private static DiaryEntryCreateDto validCreateDto(String description) {
        return validCreateDto(
                description,
                Instant.parse("2026-02-10T10:00:00Z"),
                Instant.parse("2026-02-10T10:10:00Z")
        );
    }

    private static DiaryEntryCreateDto validCreateDto(String description, Instant started, Instant ended) {
        DiaryEntryCreateDto dto = new DiaryEntryCreateDto();
        dto.setWhenStarted(started);
        dto.setWhenEnded(ended);
        dto.setMood((short) 3);
        dto.setDescription(description);
        dto.setMetrics(null);
        return dto;
    }

    private static User userWithId(Long id) {
        User user = User.builder().username("user").build();
        user.setId(id);
        return user;
    }

    private static EntryMetricCreateDto metricCreate(Long metricTypeId, Long unitId, Integer value) {
        EntryMetricValueCreateDto valueDto = new EntryMetricValueCreateDto();
        valueDto.setUnitId(unitId);
        valueDto.setValue(value);

        EntryMetricCreateDto metricDto = new EntryMetricCreateDto();
        metricDto.setMetricTypeId(metricTypeId);
        metricDto.setValues(List.of(valueDto));
        return metricDto;
    }

    private static EntryMetricUpdateDto metricUpdate(Long metricTypeId, Long unitId, Integer value) {
        EntryMetricValueUpdateDto valueDto = new EntryMetricValueUpdateDto();
        valueDto.setUnitId(unitId);
        valueDto.setValue(value);

        EntryMetricUpdateDto metricDto = new EntryMetricUpdateDto();
        metricDto.setMetricTypeId(metricTypeId);
        metricDto.setValues(List.of(valueDto));
        return metricDto;
    }

    private static DictionaryItem dictionaryItem(Long id, DictionaryType type) {
        DictionaryItem item = DictionaryItem.builder()
                .type(type)
                .label("item-" + id)
                .build();
        item.setId(id);
        return item;
    }

    private static DiaryEntry entryForUser(User user, Instant started, Instant ended) {
        DiaryEntry entry = DiaryEntry.builder()
                .user(user)
                .whenStarted(started)
                .whenEnded(ended)
                .duration(10)
                .status(EntryStatus.FAILED)
                .description("old")
                .build();
        entry.setId(1L);
        return entry;
    }
}
