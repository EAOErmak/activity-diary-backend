package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "template_entry_item",
        uniqueConstraints = {
                // В рамках одного DAY-template позиция уникальна
                @UniqueConstraint(name = "uk_tpl_entry_item_pos", columnNames = {"template_id", "position"})
        },
        indexes = {
                @Index(name = "idx_tpl_entry_item_tpl", columnList = "template_id"),
                @Index(name = "idx_tpl_entry_item_entry", columnList = "entry_template_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateEntryItem extends BaseEntity {

    /**
     * Ссылка на Template.
     * По смыслу этот template должен быть type=DAY (проверяется в сервисе).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    /** Шаблон записи (твоя DiaryEntryTemplate с name/tags/metrics...) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entry_template_id", nullable = false)
    private DiaryEntryTemplate entryTemplate;

    /** Порядок в дне: 1..N */
    @Column(nullable = false)
    private Integer position;
}

