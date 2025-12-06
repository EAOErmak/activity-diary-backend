package com.example.activity_diary.dto.analytics;

import lombok.Data;

import java.util.List;

@Data
public class ChartSeriesDto {
    private String label;
    private List<ChartPointDto> points;
}


