package com.example.activity_diary.entity.goal;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.DictionaryItem;
import jakarta.persistence.*;
import lombok.*;

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

    // РѕР¶РёРґР°РµРјРѕРµ Р·РЅР°С‡РµРЅРёРµ (snapshot)
    @Column(name = "expected_value", nullable = false)
    private Integer expectedValue;

    /* ---------- FACTORY ---------- */

    static EntryMetricValueGoal create(EntryMetricGoal metricGoal, DictionaryItem unit, Integer expectedValue) {
        if (metricGoal == null) throw new IllegalArgumentException("EntryMetricGoal is required");
        if (unit == null) throw new IllegalArgumentException("Unit is required");
        if (expectedValue == null || expectedValue <= 0)
            throw new IllegalArgumentException("Expected value must be positive");

        EntryMetricValueGoal v = new EntryMetricValueGoal();
        v.metricGoal = metricGoal;
        v.unit = unit;
        v.expectedValue = expectedValue;
        return v;
    }

    /* ---------- DOMAIN ---------- */

    public void changeExpectedValue(Integer newExpected) {
        if (newExpected == null || newExpected <= 0)
            throw new IllegalArgumentException("Expected value must be positive");
        this.expectedValue = newExpected;
    }

    void attachTo(EntryMetricGoal goal) {
        this.metricGoal = goal;
    }

    void detach() {
        this.metricGoal = null;
    }
}
