package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.EntryFieldConfigDto;
import com.example.activity_diary.dto.mapper.EntryFieldConfigMapper;
import com.example.activity_diary.entity.diary.EntryFieldConfig;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.diary.DictionaryRepository;
import com.example.activity_diary.repository.diary.EntryFieldConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntryFieldConfigServiceImplTest {

    @Mock
    private DictionaryRepository dictionaryRepository;

    @Mock
    private EntryFieldConfigMapper mapper;

    @Mock
    private EntryFieldConfigRepository entryFieldConfigRepository;

    @InjectMocks
    private EntryFieldConfigServiceImpl service;

    @Test
    void getAll_mapsAll() {
        EntryFieldConfig c1 = new EntryFieldConfig();
        EntryFieldConfig c2 = new EntryFieldConfig();

        EntryFieldConfigDto d1 = new EntryFieldConfigDto();
        d1.setName("one");
        EntryFieldConfigDto d2 = new EntryFieldConfigDto();
        d2.setName("two");

        when(entryFieldConfigRepository.findAll()).thenReturn(List.of(c1, c2));
        when(mapper.toDto(c1)).thenReturn(d1);
        when(mapper.toDto(c2)).thenReturn(d2);

        List<EntryFieldConfigDto> result = service.getAll();

        assertEquals(2, result.size());
        assertEquals("one", result.get(0).getName());
        assertEquals("two", result.get(1).getName());
    }

    @Test
    void get_categoryMissing_throwsNotFound() {
        when(dictionaryRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.get(100L));
    }

    @Test
    void get_noConfig_returnsDefault() {
        DictionaryItem category = new DictionaryItem();
        category.setEntryFieldConfig(null);
        when(dictionaryRepository.findById(1L)).thenReturn(Optional.of(category));

        EntryFieldConfigDto dto = service.get(1L);

        assertTrue(dto.getShowMetrics());
        assertTrue(dto.getShowMood());
        assertTrue(dto.getShowDescription());
        assertFalse(dto.getRequiredSubCategory());
        assertFalse(dto.getRequiredMetrics());
    }

    @Test
    void get_hasConfig_mapsConfig() {
        EntryFieldConfig config = new EntryFieldConfig();
        DictionaryItem category = new DictionaryItem();
        category.setEntryFieldConfig(config);

        EntryFieldConfigDto mapped = new EntryFieldConfigDto();
        mapped.setName("cfg");

        when(dictionaryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(mapper.toDto(config)).thenReturn(mapped);

        EntryFieldConfigDto result = service.get(1L);

        assertEquals("cfg", result.getName());
    }
}
