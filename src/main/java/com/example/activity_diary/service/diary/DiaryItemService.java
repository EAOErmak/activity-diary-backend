package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.diary.ActivityItemCreateDto;
import com.example.activity_diary.dto.diary.ActivityItemUpdateDto;
import com.example.activity_diary.entity.DiaryEntry;

import java.util.List;

public interface DiaryItemService {
    void applyItems(List<ActivityItemCreateDto> dtos, DiaryEntry entry);
    void updateItems(List<ActivityItemUpdateDto> dtos, DiaryEntry entry);
}
