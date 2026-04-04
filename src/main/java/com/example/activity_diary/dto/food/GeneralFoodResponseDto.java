package com.example.activity_diary.dto.food;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GeneralFoodResponseDto {
    private Long id;
    private Long dictionaryItemId;
    private String dictionaryItemLabel;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;
}
