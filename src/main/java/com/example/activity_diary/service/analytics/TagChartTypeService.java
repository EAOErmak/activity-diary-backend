package com.example.activity_diary.service.analytics;

import com.example.activity_diary.entity.enums.ChartType;

import java.util.List;

public interface TagChartTypeService {

    List<ChartType> getChartTypesByTagId(Long tagId);

    void validateChartTypeAllowed(Long tagId, ChartType chartType);
}
