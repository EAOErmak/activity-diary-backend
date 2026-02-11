package com.example.activity_diary.entity.template;

import com.example.activity_diary.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "template_day_item",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_day_item_dayofweek", columnNames = {"week_template_id", "day_of_week"}),
                @UniqueConstraint(name = "uk_day_item_unique_day", columnNames = {"week_template_id", "day_template_id"})
        },
        indexes = {
                @Index(name = "idx_day_item_week_tpl", columnList = "week_template_id"),
                @Index(name = "idx_day_item_day_tpl", columnList = "day_template_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateDayItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "week_template_id", nullable = false)
    private WeekTemplate weekTemplate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_template_id", nullable = false)
    private DayTemplate dayTemplate;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;
}
