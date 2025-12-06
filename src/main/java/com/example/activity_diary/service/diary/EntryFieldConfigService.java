package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.diary.EntryFieldConfigDto;

public interface EntryFieldConfigService {
    EntryFieldConfigDto get(Long whatHappenedId);
}
