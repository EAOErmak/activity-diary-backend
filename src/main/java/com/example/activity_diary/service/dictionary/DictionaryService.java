// src/main/java/com/example/activity_diary/service/dictionary/DictionaryService.java
package com.example.activity_diary.service.dictionary;

import com.example.activity_diary.dto.dictionary.DictionaryCreateDto;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryUpdateDto;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.Role;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface DictionaryService {

    DictionaryResponseDto create(DictionaryCreateDto dto);

    List<DictionaryResponseDto> getForUser(
            DictionaryType type,
            Long parentId,
            Role role
    );

    List<DictionaryResponseDto> getByTypeForAdmin(DictionaryType type);

    DictionaryResponseDto update(Long id, DictionaryUpdateDto dto);

    List<DictionaryResponseDto> search(String query, Role role);

    List<DictionaryResponseDto> searchForAdmin(String query);

    List<DictionaryResponseDto> getAll(Role role);
}

