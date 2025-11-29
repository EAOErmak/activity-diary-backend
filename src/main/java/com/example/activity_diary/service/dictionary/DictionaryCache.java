package com.example.activity_diary.service.dictionary;

import com.example.activity_diary.entity.dict.ActivityItemNameDict;
import com.example.activity_diary.entity.dict.ActivityItemUnitDict;
import com.example.activity_diary.entity.dict.WhatDict;
import com.example.activity_diary.entity.dict.WhatHappenedDict;
import com.example.activity_diary.repository.ActivityItemNameDictRepository;
import com.example.activity_diary.repository.ActivityItemUnitDictRepository;
import com.example.activity_diary.repository.WhatDictRepository;
import com.example.activity_diary.repository.WhatHappenedDictRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized dictionary cache to avoid N+1 fetches.
 * Keeps in-memory maps for fast lookup.
 */
@Component
@RequiredArgsConstructor
public class DictionaryCache {

    private final WhatHappenedDictRepository whatHappenedRepo;
    private final WhatDictRepository whatRepo;
    private final ActivityItemNameDictRepository itemNameRepo;
    private final ActivityItemUnitDictRepository unitRepo;

    // ======= Cache maps =======
    private final Map<Long, WhatHappenedDict> whatHappenedMap = new ConcurrentHashMap<>();
    private final Map<Long, WhatDict> whatMap = new ConcurrentHashMap<>();
    private final Map<Long, ActivityItemNameDict> itemNameMap = new ConcurrentHashMap<>();
    private final Map<Long, ActivityItemUnitDict> unitMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void load() {
        reloadAll();
    }

    // ======= PUBLIC API =======

    public WhatHappenedDict getWhatHappened(Long id) {
        return whatHappenedMap.get(id);
    }

    public WhatDict getWhat(Long id) {
        return whatMap.get(id);
    }

    public ActivityItemNameDict getItemName(Long id) {
        return itemNameMap.get(id);
    }

    public ActivityItemUnitDict getUnit(Long id) {
        return unitMap.get(id);
    }

    public List<WhatHappenedDict> getAllWhatHappened() {
        return List.copyOf(whatHappenedMap.values());
    }

    public List<WhatDict> getWhatByParent(Long parentId) {
        List<WhatDict> list = new ArrayList<>();
        for (WhatDict d : whatMap.values()) {
            if (d.getWhatHappenedId().equals(parentId)) {
                list.add(d);
            }
        }
        return list;
    }

    public List<ActivityItemNameDict> getAllItemNames() {
        return List.copyOf(itemNameMap.values());
    }

    public List<ActivityItemUnitDict> getAllUnits() {
        return List.copyOf(unitMap.values());
    }

    // ======= CACHE UPDATE METHODS =======

    public void reloadAll() {
        whatHappenedMap.clear();
        whatMap.clear();
        itemNameMap.clear();
        unitMap.clear();

        whatHappenedRepo.findAll().forEach(d -> whatHappenedMap.put(d.getId(), d));
        whatRepo.findAll().forEach(d -> whatMap.put(d.getId(), d));
        itemNameRepo.findAll().forEach(d -> itemNameMap.put(d.getId(), d));
        unitRepo.findAll().forEach(d -> unitMap.put(d.getId(), d));
    }

    public void insert(WhatHappenedDict d) { whatHappenedMap.put(d.getId(), d); }
    public void insert(WhatDict d) { whatMap.put(d.getId(), d); }
    public void insert(ActivityItemNameDict d) { itemNameMap.put(d.getId(), d); }
    public void insert(ActivityItemUnitDict d) { unitMap.put(d.getId(), d); }

    public void update(WhatHappenedDict d) { insert(d); }
    public void update(WhatDict d) { insert(d); }
    public void update(ActivityItemNameDict d) { insert(d); }
    public void update(ActivityItemUnitDict d) { insert(d); }

    public void removeWhatHappened(Long id) { whatHappenedMap.remove(id); }
    public void removeWhat(Long id) { whatMap.remove(id); }
    public void removeItemName(Long id) { itemNameMap.remove(id); }
    public void removeUnit(Long id) { unitMap.remove(id); }
}
