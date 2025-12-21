package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.diary.EntryFieldConfigDto;
import java.util.List;

public interface EntryFieldConfigService {
    EntryFieldConfigDto get(Long whatHappenedId);

    List<EntryFieldConfigDto> getAll();
}
