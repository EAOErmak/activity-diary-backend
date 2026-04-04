package com.example.activity_diary.service.food;

import com.example.activity_diary.dto.food.FoodUpsertDto;
import com.example.activity_diary.dto.food.UserFoodResponseDto;

import java.util.List;

public interface UserFoodService {

    List<UserFoodResponseDto> getAll(Long userId, String q);

    UserFoodResponseDto getById(Long userId, Long id);

    UserFoodResponseDto create(Long userId, FoodUpsertDto dto);

    UserFoodResponseDto update(Long userId, Long id, FoodUpsertDto dto);

    void delete(Long userId, Long id);
}
