package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.EntryFieldConfigDto;
import com.example.activity_diary.dto.mapper.EntryFieldConfigMapper;
import com.example.activity_diary.entity.EntryFieldConfig;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.DictionaryRepository;
import com.example.activity_diary.service.diary.EntryFieldConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntryFieldConfigServiceImpl implements EntryFieldConfigService {

    private final DictionaryRepository dictionaryRepository;
    private final EntryFieldConfigMapper mapper;

    @Override
    public EntryFieldConfigDto get(Long categoryId) {

        DictionaryItem category = dictionaryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        EntryFieldConfig config = category.getEntryFieldConfig();

        if (config == null) {
            return buildDefaultConfig();
        }

        return mapper.toDto(config);
    }

    private EntryFieldConfigDto buildDefaultConfig() {
        EntryFieldConfigDto dto = new EntryFieldConfigDto();

        dto.setShowSubCategory(true);
        dto.setShowMetrics(true);
        dto.setShowMood(true);
        dto.setShowDescription(true);
        dto.setRequiredSubCategory(false);
        dto.setRequiredMetrics(false);

        return dto;
    }
}
