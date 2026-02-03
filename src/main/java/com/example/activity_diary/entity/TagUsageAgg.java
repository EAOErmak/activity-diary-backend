package com.example.activity_diary.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tag_usage_agg")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TagUsageAgg {

    @EmbeddedId
    private TagUsageAggId id;

    @Column(name = "usage_count", nullable = false)
    private int usageCount;

    @Column(name = "total_duration_minutes", nullable = false)
    private long totalDurationMinutes;
}
