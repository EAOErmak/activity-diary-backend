package com.example.activity_diary.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartPointDto {

    /**
     * Ось X:
     * - в режиме ВРЕМЕНИ -> "2025-12-03"
     * - в режиме ПОСЛЕДОВАТЕЛЬНОСТИ -> "1", "2", "3"
     */
    private String x;

    /**
     * Ось Y:
     * - количество повторений
     * - часы сна
     * - граммы еды
     * и т.д.
     */
    private Double y;
}
