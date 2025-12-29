package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.diary.EntryMetricCreateDto;
import com.example.activity_diary.dto.diary.EntryMetricUpdateDto;
import com.example.activity_diary.entity.DiaryEntry;

import java.util.List;

public interface DiaryItemService {

    void applyOnCreate(List<EntryMetricCreateDto> dtos, DiaryEntry entry);
    void applyOnUpdate(List<EntryMetricUpdateDto> dtos, DiaryEntry entry);
}
