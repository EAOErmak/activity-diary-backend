package com.example.activity_diary.entity.template;

import com.example.activity_diary.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "template_entry_item",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_entry_item_pos", columnNames = {"day_template_id", "position"})
        },
        indexes = {
                @Index(name = "idx_entry_item_day_tpl", columnList = "day_template_id"),
                @Index(name = "idx_entry_item_entry_tpl", columnList = "entry_template_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateEntryItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_template_id", nullable = false)
    private DayTemplate dayTemplate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entry_template_id", nullable = false)
    private DiaryEntryTemplate entryTemplate;

    @Column(nullable = false)
    private Integer position;
}
