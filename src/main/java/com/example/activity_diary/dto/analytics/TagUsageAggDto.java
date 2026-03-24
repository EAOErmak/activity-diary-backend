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
public class TagUsageAggDto {
    private Long tagId;
    private String tagName;
    private TagUsageBucket bucket;
    private LocalDate bucketStart;
    private int usageCount;
    private long totalDurationMinutes;
}
