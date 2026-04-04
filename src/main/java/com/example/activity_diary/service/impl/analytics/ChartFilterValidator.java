package com.example.activity_diary.service.impl.analytics;

import com.example.activity_diary.dto.analytics.ChartFilterDto;
import com.example.activity_diary.exception.types.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class ChartFilterValidator {

    public void validate(ChartFilterDto filter) {
        if (filter == null) {
            throw new BadRequestException("Chart filter is required");
        }

        if (filter.getTagId() == null) {
            throw new BadRequestException("tagId is required");
        }

        if (filter.getChartType() == null) {
            throw new BadRequestException("chartType is required");
        }
    }
}
