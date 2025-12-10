package com.example.activity_diary.dto.diary;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntryMetricUpdateDto {

    // ✅ ID метрики
    @NotNull(message = "id is required for updating metric")
    @Positive(message = "id must be positive")
    private Long id;

    // ✅ Что измеряем (подход, гречка, бег и т.п.)
    @NotNull(message = "metricId is required")
    @Positive(message = "metricId must be positive")
    private Long metricId;

    // ✅ Единица измерения (раз, грамм, км)
    @NotNull(message = "unitId is required")
    @Positive(message = "unitId must be positive")
    private Long unitId;

    // ✅ Значение (сколько раз, сколько грамм)
    @NotNull(message = "value is required")
    @Positive(message = "value must be at least 1")
    private Integer value;
}
