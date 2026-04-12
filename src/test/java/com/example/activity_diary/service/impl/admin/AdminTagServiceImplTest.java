package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.diary.TagCreateDto;
import com.example.activity_diary.dto.diary.TagDto;
import com.example.activity_diary.dto.mapper.TagMapper;
import com.example.activity_diary.entity.diary.Tag;
import com.example.activity_diary.entity.enums.GlobalSyncEntityType;
import com.example.activity_diary.entity.enums.TagStatus;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.repository.tag.TagRepository;
import com.example.activity_diary.service.impl.diary.DiaryDescriptionTagPolicy;
import com.example.activity_diary.service.sync.GlobalSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private GlobalSyncService globalSyncService;

    @Mock
    private TagMapper tagMapper;

    @Spy
    private DiaryDescriptionTagPolicy diaryDescriptionTagPolicy = new DiaryDescriptionTagPolicy();

    @InjectMocks
    private AdminTagServiceImpl service;

    @Test
    void create_savesApprovedTagAndBumpsSync() {
        TagCreateDto dto = new TagCreateDto();
        dto.setName("  #Sport!! ");

        Tag saved = Tag.builder()
                .name("#sport!!")
                .status(TagStatus.APPROVED)
                .build();
        TagDto expected = new TagDto();

        when(tagRepository.findByName("#sport!!")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenReturn(saved);
        when(tagMapper.toDto(saved)).thenReturn(expected);

        TagDto actual = service.create(dto);

        assertSame(expected, actual);
        verify(tagRepository).findByName("#sport!!");
        verify(tagRepository).save(any(Tag.class));
        verify(globalSyncService).bump(GlobalSyncEntityType.TAG);
        verify(tagMapper).toDto(saved);
    }

    @Test
    void create_duplicateTag_throwsBadRequest() {
        TagCreateDto dto = new TagCreateDto();
        dto.setName("#sport");

        when(tagRepository.findByName("#sport")).thenReturn(Optional.of(Tag.builder().name("#sport").build()));

        assertThrows(BadRequestException.class, () -> service.create(dto));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void create_nameWithoutHashtag_throwsBadRequest() {
        TagCreateDto dto = new TagCreateDto();
        dto.setName("sport");

        assertThrows(BadRequestException.class, () -> service.create(dto));
        verify(tagRepository, never()).findByName(any());
        verify(tagRepository, never()).save(any(Tag.class));
    }
}
