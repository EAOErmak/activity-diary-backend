package com.example.activity_diary.dto.food;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FoodUpsertDto {

    @NotNull
    @Positive
    private Long dictionaryItemId;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal protein;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal fat;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal carbs;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal callories;
}
