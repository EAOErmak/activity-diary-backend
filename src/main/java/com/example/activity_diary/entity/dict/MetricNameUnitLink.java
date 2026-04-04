package com.example.activity_diary.entity.dict;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.enums.DictionaryType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "metric_name_unit_link",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_metric_name_unit_link_metric_name_metric_unit",
                        columnNames = {"metric_name_id", "metric_unit_id"}
                )
        },
        indexes = {
                @Index(name = "idx_metric_name_unit_link_metric_name", columnList = "metric_name_id"),
                @Index(name = "idx_metric_name_unit_link_metric_unit", columnList = "metric_unit_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricNameUnitLink extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_name_id", nullable = false)
    private DictionaryItem metricName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_unit_id", nullable = false)
    private DictionaryItem metricUnit;

    public static MetricNameUnitLink create(DictionaryItem metricName, DictionaryItem metricUnit) {
        validate(metricName, metricUnit);

        MetricNameUnitLink link = new MetricNameUnitLink();
        link.metricName = metricName;
        link.metricUnit = metricUnit;
        return link;
    }

    @PrePersist
    @PreUpdate
    private void validateLink() {
        validate(metricName, metricUnit);
    }

    private static void validate(DictionaryItem metricName, DictionaryItem metricUnit) {
        if (metricName == null) {
            throw new IllegalArgumentException("Metric name is required");
        }

        if (metricUnit == null) {
            throw new IllegalArgumentException("Metric unit is required");
        }

        if (metricName.getType() != DictionaryType.METRIC_NAME) {
            throw new IllegalArgumentException("metricName must be of type METRIC_NAME");
        }

        if (metricUnit.getType() != DictionaryType.METRIC_UNIT) {
            throw new IllegalArgumentException("metricUnit must be of type METRIC_UNIT");
        }
    }
}
