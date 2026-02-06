package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.DictionaryItem;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "entry_template_metric_value",
        uniqueConstraints = {
                // unit уникален в рамках одной templateMetric
                @UniqueConstraint(name = "uk_tpl_metric_value_unit", columnNames = {"template_metric_id", "unit_id"})
        },
        indexes = {
                @Index(name = "idx_tpl_metric_value_metric", columnList = "template_metric_id"),
                @Index(name = "idx_tpl_metric_value_unit", columnList = "unit_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EntryTemplateMetricValue extends BaseEntity {

    /** К какой метрике-шаблону относится значение */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_metric_id", nullable = false)
    private EntryTemplateMetric templateMetric;

    /** Единица измерения (мл, раз, км и т.п.) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private DictionaryItem unit;

    /** Значение в выбранных единицах */
    @Column(nullable = false)
    private Integer value;

    /* ---------- FACTORY ---------- */

    static EntryTemplateMetricValue create(
            EntryTemplateMetric templateMetric,
            DictionaryItem unit,
            Integer value
    ) {
        if (templateMetric == null)
            throw new IllegalArgumentException("TemplateMetric is required");

        if (unit == null)
            throw new IllegalArgumentException("Unit is required");

        if (value == null || value <= 0)
            throw new IllegalArgumentException("Value must be positive");

        EntryTemplateMetricValue v = new EntryTemplateMetricValue();
        v.templateMetric = templateMetric;
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
