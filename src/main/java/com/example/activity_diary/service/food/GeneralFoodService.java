package com.example.activity_diary.service.food;

import com.example.activity_diary.dto.food.FoodUpsertDto;
import com.example.activity_diary.dto.food.GeneralFoodResponseDto;

import java.util.List;

public interface GeneralFoodService {

    List<GeneralFoodResponseDto> getAll(String q);

    GeneralFoodResponseDto getById(Long id);

    GeneralFoodResponseDto create(FoodUpsertDto dto);

    GeneralFoodResponseDto update(Long id, FoodUpsertDto dto);

    void delete(Long id);
}
