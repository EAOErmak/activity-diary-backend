package com.example.activity_diary.entity.goal;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.DictionaryItem;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "entry_metric_goal",
        indexes = {
                @Index(name = "idx_metric_goal_entry_goal", columnList = "entry_goal_id"),
                @Index(name = "idx_metric_goal_type", columnList = "metric_type_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryMetricGoal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entry_goal_id", nullable = false)
    private DiaryEntryGoal entryGoal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_type_id", nullable = false)
    private DictionaryItem metricType;

    @OneToMany(
            mappedBy = "metricGoal",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<EntryMetricValueGoal> values = new ArrayList<>();

    public static EntryMetricGoal create(DiaryEntryGoal goal, DictionaryItem metricType) {
        if (goal == null) throw new IllegalArgumentException("DiaryEntryGoal is required");
        if (metricType == null) throw new IllegalArgumentException("Metric type is required");

        EntryMetricGoal mg = new EntryMetricGoal();
        mg.metricType = metricType;
        mg.attachTo(goal);
        return mg;
    }

    public void addValue(DictionaryItem unit, Integer expectedValue) {
        if (unit == null) throw new IllegalArgumentException("Unit is required");
        if (expectedValue == null || expectedValue <= 0)
            throw new IllegalArgumentException("Expected value must be positive");

        boolean exists = values.stream()
                .anyMatch(v -> Objects.equals(v.getUnit().getId(), unit.getId()));

        if (exists) throw new IllegalStateException("Unit already exists for this metric goal");

        EntryMetricValueGoal v = EntryMetricValueGoal.create(this, unit, expectedValue);
        values.add(v);
    }

    void attachTo(DiaryEntryGoal goal) { this.entryGoal = goal; }
    void detach() { this.entryGoal = null; }
}
