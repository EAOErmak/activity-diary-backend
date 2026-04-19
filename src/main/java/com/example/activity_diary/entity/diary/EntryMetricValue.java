package com.example.activity_diary.entity.diary;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.util.MetricValueNormalizer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "entry_metric_value",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"entry_metric_id", "unit_id"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EntryMetricValue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entry_metric_id", nullable = false)
    private EntryMetric entryMetric;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private DictionaryItem unit;

    @Column(nullable = false, precision = 19, scale = 5)
    private BigDecimal value;

    /* ---------- FACTORY ---------- */

    static EntryMetricValue create(
            EntryMetric entryMetric,
            DictionaryItem unit,
            BigDecimal value
    ) {
        if (entryMetric == null)
            throw new IllegalArgumentException("EntryMetric is required");

        if (unit == null)
            throw new IllegalArgumentException("Unit is required");

        EntryMetricValue v = new EntryMetricValue();
        v.entryMetric = entryMetric;
        v.unit = unit;
        v.value = MetricValueNormalizer.normalizePositive(value, "Value");
        return v;
    }

    /* ---------- DOMAIN ---------- */

    public void changeValue(BigDecimal newValue) {
        this.value = MetricValueNormalizer.normalizePositive(newValue, "Value");
    }
}

