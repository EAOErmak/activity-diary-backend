package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.entity.Tag;
import com.example.activity_diary.entity.enums.GlobalSyncEntityType;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.service.sync.GlobalSyncService;
import com.example.activity_diary.dto.mapper.TagMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private GlobalSyncService globalSyncService;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    void approve_updatesStatusAndBumpsSync() {
        Tag tag = Tag.builder().status(TagStatus.PROPOSED).build();
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        tagService.approve(1L);

        assertEquals(TagStatus.APPROVED, tag.getStatus());
        verify(globalSyncService).bump(GlobalSyncEntityType.TAG);
    }

    @Test
    void reject_updatesStatusAndBumpsSync() {
        Tag tag = Tag.builder().status(TagStatus.PROPOSED).build();
        when(tagRepository.findById(2L)).thenReturn(Optional.of(tag));

        tagService.reject(2L);

        assertEquals(TagStatus.REJECTED, tag.getStatus());
        verify(globalSyncService).bump(GlobalSyncEntityType.TAG);
    }

    @Test
    void deprecate_updatesStatusAndBumpsSync() {
        Tag tag = Tag.builder().status(TagStatus.PROPOSED).build();
        when(tagRepository.findById(3L)).thenReturn(Optional.of(tag));

        tagService.deprecate(3L);

        assertEquals(TagStatus.DEPRECATED, tag.getStatus());
        verify(globalSyncService).bump(GlobalSyncEntityType.TAG);
    }

    @Test
    void approve_whenTagMissing_throwsNotFound() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tagService.approve(99L));
    }
}
