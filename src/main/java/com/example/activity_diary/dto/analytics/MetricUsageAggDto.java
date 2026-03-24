package com.example.activity_diary.dto.analytics;

import com.example.activity_diary.entity.enums.TagUsageBucket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetricUsageAggDto {
    private Long metricTypeId;
    private String metricTypeLabel;
    private Long unitId;
    private String unitLabel;
    private TagUsageBucket bucket;
    private LocalDate bucketStart;
    private long valueSum;
    private int valueCount;
}
