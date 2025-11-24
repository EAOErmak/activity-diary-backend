package com.example.activity_diary.service.impl.dictionary;

import com.example.activity_diary.dto.dictionary.DictionaryCreateDto;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryUpdateDto;
import com.example.activity_diary.dto.mapper.DictionaryMapper;
import com.example.activity_diary.entity.dict.*;
import com.example.activity_diary.exception.types.BadRequestException;
import com.example.activity_diary.exception.types.NotFoundException;
import com.example.activity_diary.repository.ActivityItemNameDictRepository;
import com.example.activity_diary.repository.ActivityItemUnitDictRepository;
import com.example.activity_diary.repository.WhatDictRepository;
import com.example.activity_diary.repository.WhatHappenedDictRepository;
import com.example.activity_diary.service.dictionary.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class DictionaryServiceImpl implements com.example.activity_diary.service.dictionary.DictionaryService {

    private final WhatHappenedDictRepository whatHappenedRepo;
    private final WhatDictRepository whatRepo;
    private final ActivityItemNameDictRepository nameRepo;
    private final ActivityItemUnitDictRepository unitRepo;

    private final DictionaryValidator validator;
    private final DictionaryCache cache;
    private final DictionaryMapper mapper;
    private final DictionaryResolver resolver;

    @Override
    public DictionaryResponseDto createWhatHappened(DictionaryCreateDto dto) {
        String name = validator.validateName(dto.getName());
        if (whatHappenedRepo.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("WhatHappened with this name already exists");
        }
        WhatHappenedDict ent = WhatHappenedDict.builder()
                .name(name)
                .isActive(Boolean.TRUE)
                .build();
        WhatHappenedDict saved = whatHappenedRepo.save(ent);
        cache.insert(saved);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryResponseDto> getAllWhatHappened() {
        return cache.getAllWhatHappened().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public DictionaryResponseDto createWhat(Long parentId, DictionaryCreateDto dto) {

        WhatHappenedDict parent = resolver.getWhatHappened(parentId);

        String name = validator.validateName(dto.getName());

        // ✔ вызываем правильный метод
        if (whatRepo.existsByNameIgnoreCaseAndWhatHappenedId(name, parent.getId())) {
            throw new BadRequestException("What with this name already exists under parent");
        }

        WhatDict ent = WhatDict.builder()
                .name(name)
                .whatHappened(parent)
                .isActive(Boolean.TRUE)
                .build();

        WhatDict saved = whatRepo.save(ent);

        cache.insert(saved);

        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryResponseDto> getWhatByParent(Long parentId) {
        resolver.getWhatHappened(parentId); // validation
        return cache.getWhatByParent(parentId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public DictionaryResponseDto createItemName(DictionaryCreateDto dto) {
        String name = validator.validateName(dto.getName());
        if (nameRepo.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Item name already exists");
        }
        ActivityItemNameDict ent = ActivityItemNameDict.builder()
                .name(name)
                .isActive(Boolean.TRUE)
                .build();
        ActivityItemNameDict saved = nameRepo.save(ent);
        cache.insert(saved);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryResponseDto> getAllItemNames() {
        return cache.getAllItemNames().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public DictionaryResponseDto createUnit(DictionaryCreateDto dto) {
        String name = validator.validateName(dto.getName());
        if (unitRepo.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Unit already exists");
        }
        ActivityItemUnitDict ent = ActivityItemUnitDict.builder()
                .name(name)
                .isActive(Boolean.TRUE)
                .build();
        ActivityItemUnitDict saved = unitRepo.save(ent);
        cache.insert(saved);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryResponseDto> getAllUnits() {
        return cache.getAllUnits().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public DictionaryResponseDto updateItemName(Long id, DictionaryUpdateDto dto) {
        ActivityItemNameDict ent = nameRepo.findById(id).orElseThrow(() -> new NotFoundException("Item not found"));
        applyUpdate(ent, dto);
        ActivityItemNameDict saved = nameRepo.save(ent);
        cache.update(saved);
        return mapper.toDto(saved);
    }

    @Override
    public DictionaryResponseDto updateUnit(Long id, DictionaryUpdateDto dto) {
        ActivityItemUnitDict ent = unitRepo.findById(id).orElseThrow(() -> new NotFoundException("Unit not found"));
        applyUpdate(ent, dto);
        ActivityItemUnitDict saved = unitRepo.save(ent);
        cache.update(saved);
        return mapper.toDto(saved);
    }

    @Override
    public DictionaryResponseDto updateWhatHappened(Long id, DictionaryUpdateDto dto) {
        WhatHappenedDict ent = whatHappenedRepo.findById(id).orElseThrow(() -> new NotFoundException("Category not found"));
        applyUpdate(ent, dto);
        WhatHappenedDict saved = whatHappenedRepo.save(ent);
        cache.update(saved);
        return mapper.toDto(saved);
    }

    @Override
    public DictionaryResponseDto updateWhat(Long id, DictionaryUpdateDto dto) {
        WhatDict ent = whatRepo.findById(id).orElseThrow(() -> new NotFoundException("What not found"));
        applyUpdate(ent, dto);
        WhatDict saved = whatRepo.save(ent);
        cache.update(saved);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictionaryResponseDto> search(String query) {
        if (query == null || query.trim().isEmpty()) return List.of();
        String q = query.trim();
        List<DictionaryResponseDto> byWhatHappened = whatHappenedRepo.findByNameContainingIgnoreCase(q).stream().map(mapper::toDto).collect(Collectors.toList());
        List<DictionaryResponseDto> byWhat = whatRepo.findByNameContainingIgnoreCase(q).stream().map(mapper::toDto).collect(Collectors.toList());
        List<DictionaryResponseDto> byName = nameRepo.findByNameContainingIgnoreCase(q).stream().map(mapper::toDto).collect(Collectors.toList());
        List<DictionaryResponseDto> byUnit = unitRepo.findByNameContainingIgnoreCase(q).stream().map(mapper::toDto).collect(Collectors.toList());
        return Stream.of(byWhatHappened, byWhat, byName, byUnit).flatMap(List::stream).collect(Collectors.toList());
    }

    private void applyUpdate(Object ent, DictionaryUpdateDto dto) {
        if (dto == null) return;
        if (dto.getName() != null) {
            String clean = validator.validateName(dto.getName());
            if (ent instanceof ActivityItemNameDict n) n.setName(clean);
            else if (ent instanceof ActivityItemUnitDict u) u.setName(clean);
            else if (ent instanceof WhatHappenedDict w) w.setName(clean);
            else if (ent instanceof WhatDict wd) wd.setName(clean);
        }
        if (dto.getIsActive() != null) {
            if (ent instanceof ActivityItemNameDict n) n.setIsActive(dto.getIsActive());
            else if (ent instanceof ActivityItemUnitDict u) u.setIsActive(dto.getIsActive());
            else if (ent instanceof WhatHappenedDict w) w.setIsActive(dto.getIsActive());
            else if (ent instanceof WhatDict wd) wd.setIsActive(dto.getIsActive());
        }
    }
}
