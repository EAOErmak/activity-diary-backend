package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.EntryFieldConfigDto;
import com.example.activity_diary.dto.mapper.EntryFieldConfigMapper;
import com.example.activity_diary.entity.EntryFieldConfig;
import com.example.activity_diary.repository.EntryFieldConfigRepository;
import com.example.activity_diary.service.diary.EntryFieldConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntryFieldConfigServiceImpl implements EntryFieldConfigService {

    private final EntryFieldConfigRepository configRepo;
    private final EntryFieldConfigMapper mapper;

    @Override
    public EntryFieldConfigDto get(Long whatHappenedId) {

        return configRepo
                .findByWhatHappenedId(whatHappenedId)
                .map(mapper::toDto)
                .orElseGet(this::buildDefaultConfig);
    }

    private EntryFieldConfigDto buildDefaultConfig() {
        EntryFieldConfigDto dto = new EntryFieldConfigDto();

        dto.setShowWhat(true);
        dto.setShowActivities(true);
        dto.setShowFeeling(true);
        dto.setShowDescription(true);
        dto.setRequiredWhat(false);
        dto.setRequiredActivities(false);

        return dto;
    }
}

