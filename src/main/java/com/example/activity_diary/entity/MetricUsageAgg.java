package com.example.activity_diary.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "metric_usage_agg")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MetricUsageAgg {

    @EmbeddedId
    private MetricUsageAggId id;

    @Column(name = "value_sum", nullable = false)
    private long valueSum;

    @Column(name = "value_count", nullable = false)
    private int valueCount;
}
