package com.example.activity_diary.dto.analytics;

import com.example.activity_diary.entity.enums.TagUsageBucket;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetricUsageAggFilterDto {
    @NotNull
    private TagUsageBucket bucket;
    private Long metricTypeId;
    private Long unitId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
