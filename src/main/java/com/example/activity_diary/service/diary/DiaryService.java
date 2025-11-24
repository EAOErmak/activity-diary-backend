package com.example.activity_diary.service.diary;

import com.example.activity_diary.dto.diary.DiaryEntryCreateDto;
import com.example.activity_diary.dto.diary.DiaryEntryDto;
import com.example.activity_diary.dto.diary.DiaryEntryUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;

public interface DiaryService {

    // получение списка записей текущего пользователя (новый правильный API)
    Page<DiaryEntryDto> getMyEntries(UserDetails currentUser, int page, int size);

    // получить запись (доступ только к своей)
    DiaryEntryDto getMyEntryById(Long id, UserDetails currentUser);

    // создать запись
    DiaryEntryDto create(DiaryEntryCreateDto dto, UserDetails currentUser);

    // обновить запись
    DiaryEntryDto update(Long id, DiaryEntryUpdateDto dto, UserDetails currentUser);

    // удалить запись
    void delete(Long id, UserDetails currentUser);

    // админские методы (опционально)
    // Page<DiaryEntryDto> getAll(int page, int size);
    // void deleteAll();
}
