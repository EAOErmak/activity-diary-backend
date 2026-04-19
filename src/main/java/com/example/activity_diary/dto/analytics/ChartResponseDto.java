package com.example.activity_diary.dto.analytics;

import com.example.activity_diary.entity.enums.ChartType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChartResponseDto {
    private ChartType chartType;
    private List<ChartSeriesDto> series;
}
