package com.example.activity_diary.dto.diary;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EntryMetricDto {

    // ✅ Только для ответа с сервера
    private Long id;

    // ✅ Что измеряем (подход, гречка, бег и т.п.)
    @NotNull(message = "metricId cannot be null")
    @Min(value = 1, message = "metricId must be a positive ID")
    private Long metricId;

    // ✅ Единица измерения (раз, грамм, км)
    @NotNull(message = "unitId cannot be null")
    @Min(value = 1, message = "unitId must be a positive ID")
    private Long unitId;

    // ✅ Значение (сколько раз, сколько грамм)
    @NotNull(message = "value cannot be null")
    @Min(value = 1, message = "value must be greater than zero")
    private Integer value;
}
