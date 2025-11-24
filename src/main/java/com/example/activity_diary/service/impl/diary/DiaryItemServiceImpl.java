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

    // ============================================
    //            CREATE ITEMS
    // ============================================
    @Override
    public void applyItems(List<ActivityItemCreateDto> dtos, DiaryEntry entry) {
        if (dtos == null || dtos.isEmpty()) return;

        List<ActivityItem> result = new ArrayList<>();

        for (ActivityItemCreateDto dto : dtos) {
            ActivityItem item = mapper.toActivityItem(dto);
            item.setDiaryEntry(entry);
            result.add(item);
        }

        entry.getWhatDidYouDo().clear();
        entry.getWhatDidYouDo().addAll(result);
    }

    // ============================================
    //            UPDATE ITEMS
    // ============================================
    @Override
    public void updateItems(List<ActivityItemUpdateDto> dtos, DiaryEntry entry) {

        List<ActivityItem> existing = entry.getWhatDidYouDo();
        Map<Long, ActivityItem> oldById = existing.stream()
                .filter(i -> i.getId() != null)
                .collect(Collectors.toMap(ActivityItem::getId, i -> i));

        List<ActivityItem> result = new ArrayList<>();

        if (dtos != null) {
            for (ActivityItemUpdateDto dto : dtos) {
                // UPDATE EXISTING
                if (dto.getId() != null && oldById.containsKey(dto.getId())) {

                    ActivityItem item = oldById.get(dto.getId());

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
                    oldById.remove(dto.getId());
                }

                // CREATE NEW ITEM
                else {
                    ActivityItem item = mapper.toActivityItem(
                            new ActivityItemCreateDto() {{
                                setNameId(dto.getNameId());
                                setUnitId(dto.getUnitId());
                                setCount(dto.getCount());
                            }}
                    );
                    item.setDiaryEntry(entry);
                    result.add(item);
                }
            }
        }

        // REMOVE missing items
        for (ActivityItem removed : oldById.values()) {
            existing.remove(removed);
        }

        // APPLY RESULT LIST
        existing.clear();
        existing.addAll(result);
    }
}
