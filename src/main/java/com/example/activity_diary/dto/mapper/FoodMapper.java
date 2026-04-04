package com.example.activity_diary.dto.mapper;

import com.example.activity_diary.dto.food.GeneralFoodResponseDto;
import com.example.activity_diary.dto.food.UserFoodResponseDto;
import com.example.activity_diary.entity.food.GeneralFood;
import com.example.activity_diary.entity.food.UserFood;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FoodMapper {

    @Mapping(source = "dictionaryItem.id", target = "dictionaryItemId")
    @Mapping(source = "dictionaryItem.label", target = "dictionaryItemLabel")
    GeneralFoodResponseDto toDto(GeneralFood generalFood);

    List<GeneralFoodResponseDto> toGeneralFoodDtoList(List<GeneralFood> generalFoods);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "dictionaryItem.id", target = "dictionaryItemId")
    @Mapping(source = "dictionaryItem.label", target = "dictionaryItemLabel")
    UserFoodResponseDto toDto(UserFood userFood);

    List<UserFoodResponseDto> toUserFoodDtoList(List<UserFood> userFoods);
}
