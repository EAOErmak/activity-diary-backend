package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "template_day_item",
        uniqueConstraints = {
                // В рамках одной недели позиция уникальна (обычно 1..7)
                @UniqueConstraint(name = "uk_tpl_day_item_pos", columnNames = {"template_id", "position"})
        },
        indexes = {
                @Index(name = "idx_tpl_day_item_tpl", columnList = "template_id"),
                @Index(name = "idx_tpl_day_item_day_tpl", columnList = "day_template_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDayItem extends BaseEntity {

    /**
     * Ссылка на Template типа WEEK.
     * (проверяется в сервисе)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    /**
     * Ссылка на Template типа DAY.
     * (проверяется в сервисе)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_template_id", nullable = false)
    private Template dayTemplate;

    /** Порядок: 1..7 */
    @Column(nullable = false)
    private Integer position;
}

