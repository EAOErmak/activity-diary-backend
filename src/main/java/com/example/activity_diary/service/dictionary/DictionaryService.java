package com.example.activity_diary.service.dictionary;

import com.example.activity_diary.dto.dictionary.DictionaryCreateDto;
import com.example.activity_diary.dto.dictionary.DictionaryResponseDto;
import com.example.activity_diary.dto.dictionary.DictionaryUpdateDto;

import java.util.List;

public interface DictionaryService {

    DictionaryResponseDto createWhatHappened(DictionaryCreateDto dto);

    List<DictionaryResponseDto> getAllWhatHappened();

    DictionaryResponseDto createWhat(Long parentId, DictionaryCreateDto dto);

    List<DictionaryResponseDto> getWhatByParent(Long parentId);

    DictionaryResponseDto createItemName(DictionaryCreateDto dto);

    List<DictionaryResponseDto> getAllItemNames();

    DictionaryResponseDto createUnit(DictionaryCreateDto dto);

    List<DictionaryResponseDto> getAllUnits();

    DictionaryResponseDto updateItemName(Long id, DictionaryUpdateDto dto);

    DictionaryResponseDto updateUnit(Long id, DictionaryUpdateDto dto);

    DictionaryResponseDto updateWhatHappened(Long id, DictionaryUpdateDto dto);

    DictionaryResponseDto updateWhat(Long id, DictionaryUpdateDto dto);

    List<DictionaryResponseDto> search(String query);
}
