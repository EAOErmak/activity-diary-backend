package com.example.activity_diary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "metric_usage_agg")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MetricUsageAgg {

    @EmbeddedId
    private MetricUsageAggId id;

    @Column(name = "value_sum", nullable = false, precision = 24, scale = 5)
    private BigDecimal valueSum;

    @Column(name = "value_count", nullable = false)
    private int valueCount;
}
