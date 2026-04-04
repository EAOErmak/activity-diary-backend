package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.UserTag;
import com.example.activity_diary.entity.UserTagId;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.GlobalSyncEntityType;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.repository.UserRepository;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.repository.tag.UserTagRepository;
import com.example.activity_diary.service.sync.GlobalSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagResolverServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTagRepository userTagRepository;

    @Mock
    private GlobalSyncService globalSyncService;

    @InjectMocks
    private TagResolverServiceImpl service;

    @Test
    void resolveFromDescription_extractsNormalizesAndDeduplicates() {
        User user = userWithId(1L);
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(tagRepository.findByNameIn(any())).thenReturn(List.of());
        when(userTagRepository.existsById(any())).thenReturn(false);

        AtomicLong idSeq = new AtomicLong(100);
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag tag = invocation.getArgument(0);
            if (tag.getId() == null) {
                tag.setId(idSeq.getAndIncrement());
            }
            return tag;
        });

        Set<Tag> tags = service.resolveFromDescription(
                1L,
                "Run #Sport and #sport! #test_1 #a"
        );

        assertEquals(2, tags.size());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<LinkedHashSet<String>> namesCaptor =
                ArgumentCaptor.forClass(LinkedHashSet.class);
        verify(tagRepository).findByNameIn(namesCaptor.capture());

        LinkedHashSet<String> names = namesCaptor.getValue();
        assertEquals(List.of("sport", "test_1"), List.copyOf(names));
        verify(globalSyncService, times(2)).bump(GlobalSyncEntityType.TAG);
    }

    @Test
    void resolveForUser_rejectedTag_throws() {
        User user = userWithId(2L);
        when(userRepository.getReferenceById(2L)).thenReturn(user);

        Tag rejected = Tag.builder()
                .name("bad")
                .status(TagStatus.REJECTED)
                .build();
        rejected.setId(5L);

        when(tagRepository.findByNameIn(any())).thenReturn(List.of(rejected));

        Collection<String> raw = List.of("bad");

        assertThrows(IllegalArgumentException.class, () ->
                service.resolveForUser(2L, raw));

        verify(userTagRepository, never()).save(any(UserTag.class));
    }

    @Test
    void resolveForUser_createsUserTagWhenMissing() {
        User user = userWithId(3L);
        when(userRepository.getReferenceById(3L)).thenReturn(user);

        Tag existing = Tag.builder()
                .name("ok")
                .status(TagStatus.APPROVED)
                .build();
        existing.setId(7L);

        when(tagRepository.findByNameIn(any())).thenReturn(List.of(existing));
        when(userTagRepository.existsById(any())).thenReturn(false);

        Set<Tag> tags = service.resolveForUser(3L, List.of("ok"));

        assertEquals(1, tags.size());

        ArgumentCaptor<UserTag> userTagCaptor = ArgumentCaptor.forClass(UserTag.class);
        verify(userTagRepository).save(userTagCaptor.capture());

        UserTag saved = userTagCaptor.getValue();
        UserTagId id = saved.getId();
        assertEquals(3L, id.getUserId());
        assertEquals(7L, id.getTagId());
    }

    @Test
    void resolveForUser_concurrentCreate_usesFindByName() {
        User user = userWithId(4L);
        when(userRepository.getReferenceById(4L)).thenReturn(user);
        when(tagRepository.findByNameIn(any())).thenReturn(List.of());
        when(userTagRepository.existsById(any())).thenReturn(false);

        when(tagRepository.save(any(Tag.class)))
                .thenThrow(new DataIntegrityViolationException("unique"));

        Tag existing = Tag.builder()
                .name("race")
                .status(TagStatus.PENDING)
                .build();
        existing.setId(9L);

        when(tagRepository.findByName("race")).thenReturn(Optional.of(existing));

        Set<Tag> tags = service.resolveForUser(4L, List.of("race"));

        assertEquals(1, tags.size());
        assertTrue(tags.stream().anyMatch(t -> t.getId().equals(9L)));
        verify(tagRepository).findByName("race");
    }

    private static User userWithId(Long id) {
        User user = User.builder().username("user").build();
        user.setId(id);
        return user;
    }
}
