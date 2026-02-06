package com.example.activity_diary.entity;

import com.example.activity_diary.entity.dict.DictionaryItem;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "template_goal_metric",
        indexes = {
                @Index(name = "idx_tpl_goal_metric_tpl", columnList = "template_id"),
                @Index(name = "idx_tpl_goal_metric_type", columnList = "metric_type_id"),
                @Index(name = "idx_tpl_goal_metric_unit", columnList = "unit_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateGoalMetric {

    @EmbeddedId
    private TemplateGoalMetricId id;

    @MapsId("templateId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @MapsId("metricTypeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_type_id", nullable = false)
    private DictionaryItem metricType;

    @MapsId("unitId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private DictionaryItem unit;

    @Column(name = "sum_value", nullable = false)
    private Integer sumValue;
}

