package com.example.activity_diary.service.impl.admin;

import com.example.activity_diary.dto.diary.EntryFieldConfigDto;
import com.example.activity_diary.dto.mapper.EntryFieldConfigMapper;
import com.example.activity_diary.entity.EntryFieldConfig;
import com.example.activity_diary.exception.types.NotFoundException;
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

    // ============================
    // CREATE
    // ============================

    @Override
    public EntryFieldConfigDto create(EntryFieldConfigDto dto) {

        if (repo.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Config with this name already exists");
        }

        EntryFieldConfig config = mapper.toEntity(dto);

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
        config.setShowSubCategory(dto.getShowSubCategory());
        config.setShowMetrics(dto.getShowMetrics());
        config.setShowMood(dto.getShowMood());
        config.setShowDescription(dto.getShowDescription());
        config.setRequiredSubCategory(dto.getRequiredSubCategory());
        config.setRequiredMetrics(dto.getRequiredMetrics());

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
