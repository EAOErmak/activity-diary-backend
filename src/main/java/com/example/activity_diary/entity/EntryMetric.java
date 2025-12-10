package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "entry_metric",
        indexes = {
                @Index(name = "idx_metric_entry", columnList = "diary_entry_id"),
                @Index(name = "idx_metric_type", columnList = "metric_type_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EntryMetric extends BaseEntity {

    // ✅ ЧТО измеряем (подход, гречка, бег и т.п.)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_type_id", nullable = false)
    private DictionaryItem metricType;

    // ✅ В ЧЁМ измеряем (повторы, граммы, км, минуты)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private DictionaryItem unit;

    // ✅ ЗНАЧЕНИЕ (10 повторов, 200 грамм, 5 км)
    @Column(name = "value", nullable = false)
    private Integer value;

    // ✅ К какой записи относится
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diary_entry_id", nullable = false)
    @JsonIgnore
    private DiaryEntry diaryEntry;

    // ============================================================
    // FACTORY
    // ============================================================

    public static EntryMetric create(
            DiaryEntry entry,
            DictionaryItem metricType,
            DictionaryItem unit,
            Integer value
    ) {
        if (entry == null) throw new IllegalArgumentException("DiaryEntry is required");
        if (metricType == null) throw new IllegalArgumentException("Metric type is required");
        if (unit == null) throw new IllegalArgumentException("Unit is required");
        if (value == null || value <= 0)
            throw new IllegalArgumentException("Value must be positive");

        EntryMetric metric = EntryMetric.builder()
                .metricType(metricType)
                .unit(unit)
                .value(value)
                .build();

        metric.attachTo(entry);
        return metric;
    }

    // ============================================================
    // BUSINESS METHODS
    // ============================================================

    public void changeValue(Integer newValue) {
        if (newValue == null || newValue <= 0)
            throw new IllegalArgumentException("Value must be positive");

        this.value = newValue;
    }

    public void changeMetricType(DictionaryItem newType) {
        if (newType == null)
            throw new IllegalArgumentException("Metric type is required");

        this.metricType = newType;
    }

    public void changeUnit(DictionaryItem newUnit) {
        if (newUnit == null)
            throw new IllegalArgumentException("Unit is required");

        this.unit = newUnit;
    }

    public void attachTo(DiaryEntry entry) {
        this.diaryEntry = entry;
    }

    public void detach() {
        this.diaryEntry = null;
    }
}
