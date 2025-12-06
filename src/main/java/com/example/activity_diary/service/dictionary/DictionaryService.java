// src/main/java/com/example/activity_diary/service/dictionary/DictionaryService.java
package com.example.activity_diary.service.dictionary;

import com.example.activity_diary.dto.dictionary.DictionaryCreateDto;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryUpdateDto;
import com.example.activity_diary.entity.enums.DictionaryType;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface DictionaryService {

    DictionaryResponseDto create(DictionaryCreateDto dto);

    // Для пользователя (фильтрация по ролям и active = true)
    List<DictionaryResponseDto> getByTypeOrParent(DictionaryType type, Long parentId, UserDetails ud);

    // Для админа (без фильтра по active/role)
    List<DictionaryResponseDto> getByTypeForAdmin(DictionaryType type);

    DictionaryResponseDto update(Long id, DictionaryUpdateDto dto);

    // Поиск для пользователя
    List<DictionaryResponseDto> search(String query, UserDetails ud);

    // Поиск для админа
    List<DictionaryResponseDto> searchForAdmin(String query);
}
