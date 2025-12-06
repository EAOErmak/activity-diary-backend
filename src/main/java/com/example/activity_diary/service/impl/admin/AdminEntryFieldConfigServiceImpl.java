package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.diary.EntryFieldConfigDto;
import com.example.activity_diary.dto.mapper.EntryFieldConfigMapper;
import com.example.activity_diary.entity.EntryFieldConfig;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.DictionaryRepository;
import com.example.activity_diary.repository.EntryFieldConfigRepository;
import com.example.activity_diary.service.admin.AdminEntryFieldConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEntryFieldConfigServiceImpl implements AdminEntryFieldConfigService {

    private final EntryFieldConfigRepository repo;
    private final EntryFieldConfigMapper mapper;
    private final DictionaryRepository dictionaryRepository;

    // ============================
    // CREATE
    // ============================

    @Override
    public EntryFieldConfigDto create(EntryFieldConfigDto dto) {
        EntryFieldConfig config = mapper.toEntity(dto);

        // ✅ ВРУЧНУЮ ПРИВЯЗЫВАЕМ whatHappened ПО ID
        if (dto.getWhatHappenedId() != null) {
            DictionaryItem item = dictionaryRepository.findById(dto.getWhatHappenedId())
                    .orElseThrow(() -> new NotFoundException("Dictionary item not found"));

            config.setWhatHappened(item);
        }

        return mapper.toDto(repo.save(config));
    }

    // ============================
    // UPDATE
    // ============================

    @Override
    public EntryFieldConfigDto update(Long id, EntryFieldConfigDto dto) {
        EntryFieldConfig config = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Config not found"));

        config.setName(dto.getName());
        config.setShowWhat(dto.getShowWhat());
        config.setShowActivities(dto.getShowActivities());
        config.setShowFeeling(dto.getShowFeeling());
        config.setShowDescription(dto.getShowDescription());
        config.setRequiredWhat(dto.getRequiredWhat());
        config.setRequiredActivities(dto.getRequiredActivities());

        // ✅ ЕСЛИ НАДО ПЕРЕПРИВЯЗАТЬ whatHappened
        if (dto.getWhatHappenedId() != null) {
            DictionaryItem item = dictionaryRepository.findById(dto.getWhatHappenedId())
                    .orElseThrow(() -> new NotFoundException("Dictionary item not found"));

            config.setWhatHappened(item);
        }

        return mapper.toDto(repo.save(config));
    }

    // ============================
    // GET ALL
    // ============================

    @Override
    public List<EntryFieldConfigDto> getAll() {
        return repo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // ============================
    // DELETE
    // ============================

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}

