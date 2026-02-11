package com.example.activity_diary.entity.diary;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.DictionaryItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false)
    private Integer value;

    /* ---------- FACTORY ---------- */

    static EntryMetricValue create(
            EntryMetric entryMetric,
            DictionaryItem unit,
            Integer value
    ) {
        if (entryMetric == null)
            throw new IllegalArgumentException("EntryMetric is required");

        if (unit == null)
            throw new IllegalArgumentException("Unit is required");

        if (value == null || value <= 0)
            throw new IllegalArgumentException("Value must be positive");

        EntryMetricValue v = new EntryMetricValue();
        v.entryMetric = entryMetric;
        v.unit = unit;
        v.value = value;
        return v;
    }

    /* ---------- DOMAIN ---------- */

    public void changeValue(Integer newValue) {
        if (newValue == null || newValue <= 0)
            throw new IllegalArgumentException("Value must be positive");

        this.value = newValue;
    }
}

