package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import com.example.activity_diary.dto.diary.DiaryEntryViewDto;
import com.example.activity_diary.dto.mapper.DiaryEntryMapper;
import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.diary.DiaryEntry;
import com.example.activity_diary.entity.diary.Tag;
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
        when(diaryRepository.findById(1L)).thenReturn(Optional.empty());

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
    void update_pastEntry_throwsBadRequest() {
        DiaryEntryUpdateDto dto = new DiaryEntryUpdateDto();
        User user = userWithId(10L);
        DiaryEntry entry = entryForUser(user,
                Instant.now().minusSeconds(600),
                Instant.now().minusSeconds(300));

        when(diaryRepository.findById(1L)).thenReturn(Optional.of(entry));

        assertThrows(BadRequestException.class, () -> service.update(1L, dto, 10L));
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
        when(diaryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(tagResolverService.resolveFromDescription(10L, "  new desc ")).thenReturn(Set.of(tag));
        when(diaryRepository.save(entry)).thenReturn(entry);
        DiaryEntryDto mapped = new DiaryEntryDto();
        when(mapper.toDto(entry)).thenReturn(mapped);

        DiaryEntryDto result = service.update(1L, dto, 10L);

        assertEquals(mapped, result);
        verify(userSyncService).bump(10L, UserSyncEntityType.DIARY);
    }

    private static DiaryEntryCreateDto validCreateDto(String description) {
        DiaryEntryCreateDto dto = new DiaryEntryCreateDto();
        dto.setWhenStarted(Instant.parse("2026-02-10T10:00:00Z"));
        dto.setWhenEnded(Instant.parse("2026-02-10T10:10:00Z"));
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

    private static DiaryEntry entryForUser(User user, Instant started, Instant ended) {
        DiaryEntry entry = DiaryEntry.builder()
                .user(user)
                .whenStarted(started)
                .whenEnded(ended)
                .duration(10)
                .status(EntryStatus.LOSE)
                .description("old")
                .build();
        entry.setId(1L);
        return entry;
    }
}
