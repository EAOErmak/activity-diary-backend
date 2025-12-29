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
    private DictionaryType type;

    @Column(nullable = false)
    private String label;

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
    private Role allowedRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_field_config_id")
    private EntryFieldConfig entryFieldConfig;
}
