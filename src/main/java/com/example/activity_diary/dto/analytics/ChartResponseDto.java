package com.example.activity_diary.dto.analytics;

import com.example.activity_diary.entity.enums.ChartType;
import lombok.Data;

import java.util.List;

@Data
public class ChartResponseDto {

    /** Название графика: "Тренировка", "Подтягивания", "Сон" */
    private String title;

    /** Тип логики расчёта (REPS_SUM, TIME_RANGE и т.д.) */
    private ChartType chartType;

    /** Единица измерения: "повторы", "часы", "граммы" */
    private String unit;

    /** Сами точки */
    private List<ChartPointDto> points;
}
