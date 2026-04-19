package com.example.activity_diary.entity.goal;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.util.MetricValueNormalizer;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "entry_metric_value_goal",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_metric_value_goal_metric_unit",
                        columnNames = {"metric_goal_id", "unit_id"}
                )
        },
        indexes = {
                @Index(name = "idx_metric_value_goal_metric", columnList = "metric_goal_id"),
                @Index(name = "idx_metric_value_goal_unit", columnList = "unit_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryMetricValueGoal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_goal_id", nullable = false)
    private EntryMetricGoal metricGoal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private DictionaryItem unit;

    // ожидаемое значение (snapshot)
    @Column(name = "expected_value", nullable = false, precision = 19, scale = 5)
    private BigDecimal expectedValue;

    /* ---------- FACTORY ---------- */

    static EntryMetricValueGoal create(EntryMetricGoal metricGoal, DictionaryItem unit, BigDecimal expectedValue) {
        if (metricGoal == null) throw new IllegalArgumentException("EntryMetricGoal is required");
        if (unit == null) throw new IllegalArgumentException("Unit is required");

        EntryMetricValueGoal v = new EntryMetricValueGoal();
        v.metricGoal = metricGoal;
        v.unit = unit;
        v.expectedValue = MetricValueNormalizer.normalizePositive(expectedValue, "Expected value");
        return v;
    }

    /* ---------- DOMAIN ---------- */

    public void changeExpectedValue(BigDecimal newExpected) {
        this.expectedValue = MetricValueNormalizer.normalizePositive(newExpected, "Expected value");
    }

    void attachTo(EntryMetricGoal goal) {
        this.metricGoal = goal;
    }

    void detach() {
        this.metricGoal = null;
    }
}
