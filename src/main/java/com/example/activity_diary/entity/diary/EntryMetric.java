package com.example.activity_diary.entity.diary;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
@EntityListeners(AuditingEntityListener.class)
public class EntryMetric extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_type_id", nullable = false)
    private DictionaryItem metricType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diary_entry_id", nullable = false)
    @JsonIgnore
    private DiaryEntry diaryEntry;

    @OneToMany(
            mappedBy = "entryMetric",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private final List<EntryMetricValue> values = new ArrayList<>();

    /* ---------- FACTORY ---------- */

    public static EntryMetric create(
            DiaryEntry entry,
            DictionaryItem metricType
    ) {
        if (entry == null) throw new IllegalArgumentException("DiaryEntry is required");
        if (metricType == null) throw new IllegalArgumentException("Metric type is required");

        EntryMetric metric = new EntryMetric();
        metric.metricType = metricType;
        metric.attachTo(entry);
        return metric;
    }

    /* ---------- DOMAIN LOGIC ---------- */

    public void addValue(DictionaryItem unit, BigDecimal value) {
        if (unit == null)
            throw new IllegalArgumentException("Unit is required");

        boolean exists = values.stream()
                .anyMatch(v -> Objects.equals(v.getUnit().getId(), unit.getId()));

        if (exists)
            throw new IllegalStateException("Unit already exists for this metric");

        EntryMetricValue metricValue = EntryMetricValue.create(this, unit, value);
        values.add(metricValue);
    }

    public void changeMetricType(DictionaryItem newType) {
        if (newType == null)
            throw new IllegalArgumentException("Metric type is required");

        this.metricType = newType;
    }

    void attachTo(DiaryEntry entry) {
        this.diaryEntry = entry;
    }

    void detach() {
        this.diaryEntry = null;
    }
}
