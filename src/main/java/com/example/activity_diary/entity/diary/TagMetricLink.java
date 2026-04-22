package com.example.activity_diary.entity.diary;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.DictionaryItem;
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
        name = "tag_metric_link",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tag_metric_link_tag_metric_name",
                        columnNames = {"tag_id", "metric_name_id"}
                )
        },
        indexes = {
                @Index(name = "idx_tag_metric_link_tag", columnList = "tag_id"),
                @Index(name = "idx_tag_metric_link_metric_name", columnList = "metric_name_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagMetricLink extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_name_id", nullable = false)
    private DictionaryItem metricName;

    public static TagMetricLink create(Tag tag, DictionaryItem metricName) {
        validate(tag, metricName);

        TagMetricLink link = new TagMetricLink();
        link.tag = tag;
        link.metricName = metricName;
        return link;
    }

    @PrePersist
    @PreUpdate
    private void validateLink() {
        validate(tag, metricName);
    }

    private static void validate(Tag tag, DictionaryItem metricName) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag is required");
        }

        if (metricName == null) {
            throw new IllegalArgumentException("Metric name is required");
        }

        if (metricName.getType() != DictionaryType.METRIC_NAME) {
            throw new IllegalArgumentException("metricName must be of type METRIC_NAME");
        }
    }
}
