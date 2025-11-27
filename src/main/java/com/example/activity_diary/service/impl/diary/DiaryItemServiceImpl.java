package com.example.activity_diary.service.impl.diary;

import com.example.activity_diary.dto.diary.ActivityItemCreateDto;
import com.example.activity_diary.dto.diary.ActivityItemUpdateDto;
import com.example.activity_diary.entity.ActivityItem;
import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.dto.mapper.DiaryEntryMapper;
import com.example.activity_diary.service.diary.DiaryItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryItemServiceImpl implements DiaryItemService {

    private final DiaryEntryMapper mapper;

    // =======================================================
    //                       CREATE
    // =======================================================
    @Override
    public void applyItems(List<ActivityItemCreateDto> dtos, DiaryEntry entry) {
        if (dtos == null || dtos.isEmpty()) {
            entry.getWhatDidYouDo().clear();
            return;
        }

        List<ActivityItem> items = dtos.stream()
                .map(mapper::toActivityItem)
                .peek(i -> i.setDiaryEntry(entry))
                .toList();

        // orphanRemoval = true → Hibernate удалит старые items
        entry.getWhatDidYouDo().clear();
        entry.getWhatDidYouDo().addAll(items);
    }


    // =======================================================
    //                       UPDATE
    // =======================================================
    @Override
    public void updateItems(List<ActivityItemUpdateDto> dtos, DiaryEntry entry) {

        List<ActivityItem> existing = entry.getWhatDidYouDo();

        // Map existing items: id → entity
        Map<Long, ActivityItem> oldMap = existing.stream()
                .filter(i -> i.getId() != null)
                .collect(Collectors.toMap(ActivityItem::getId, i -> i));

        List<ActivityItem> result = new ArrayList<>();

        if (dtos != null) {
            for (ActivityItemUpdateDto dto : dtos) {

                // =====================
                //    UPDATE EXISTING
                // =====================
                if (dto.getId() != null && oldMap.containsKey(dto.getId())) {

                    ActivityItem item = oldMap.get(dto.getId());

                    // update fields
                    if (dto.getNameId() != null) {
                        item.setName(mapper.mapName(dto.getNameId()));
                    }
                    if (dto.getUnitId() != null) {
                        item.setUnit(mapper.mapUnit(dto.getUnitId()));
                    }
                    if (dto.getCount() != null) {
                        item.setCount(dto.getCount());
                    }

                    result.add(item);
                    oldMap.remove(dto.getId());
                }

                // =====================
                //    CREATE NEW ITEM
                // =====================
                else {
                    ActivityItemCreateDto createDto = new ActivityItemCreateDto();
                    createDto.setNameId(dto.getNameId());
                    createDto.setUnitId(dto.getUnitId());
                    createDto.setCount(dto.getCount());

                    ActivityItem newItem = mapper.toActivityItem(createDto);
                    newItem.setDiaryEntry(entry);
                    result.add(newItem);
                }
            }
        }

        // =====================
        //     DELETE MISSING
        // =====================
        for (ActivityItem removed : oldMap.values()) {
            existing.remove(removed); // JPA orphanRemoval → DELETE SQL
        }

        // =====================
        //     APPLY RESULT
        // =====================
        existing.clear();
        existing.addAll(result);
    }
}

