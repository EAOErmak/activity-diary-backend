package com.example.activity_diary.service.admin;

import com.example.activity_diary.dto.diary.EntryFieldConfigDto;

import java.util.List;

public interface AdminEntryFieldConfigService {

    EntryFieldConfigDto create(EntryFieldConfigDto dto);

    EntryFieldConfigDto update(Long id, EntryFieldConfigDto dto);

    List<EntryFieldConfigDto> getAll();

    void delete(Long id);
}


