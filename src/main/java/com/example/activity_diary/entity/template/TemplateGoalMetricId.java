package com.example.activity_diary.entity.template;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TemplateGoalMetricId implements java.io.Serializable {
    private Long templateId;
    private Long metricTypeId;
    private Long unitId;
}

