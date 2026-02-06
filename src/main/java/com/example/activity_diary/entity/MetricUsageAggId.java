package com.example.activity_diary.entity;

import com.example.activity_diary.entity.enums.TagUsageBucket;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class MetricUsageAggId implements Serializable {

    private Long userId;
    private Long metricTypeId;
    private Long unitId;

    @Enumerated(EnumType.STRING)
    private TagUsageBucket bucket;

    private LocalDate bucketStart;
}
