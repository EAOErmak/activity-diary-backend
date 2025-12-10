package com.example.activity_diary.entity.dict;

import com.example.activity_diary.entity.EntryFieldConfig;
import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.enums.ChartType;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "dictionary_item",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"type", "label"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryItem extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DictionaryType type;   // WHAT, WHAT_HAPPENED, UNIT, ITEM_NAME

    @Column(nullable = false)
    private String label;          // текст, который видит пользователь

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DictionaryItem parent;

    @Enumerated(EnumType.STRING)
    @Column(name = "chart_type")
    private ChartType chartType;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "allowed_role", length = 50)
    private Role allowedRole;    // USER / ADMIN / PREMIUM и т.п.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_field_config_id")
    private EntryFieldConfig entryFieldConfig;
}
