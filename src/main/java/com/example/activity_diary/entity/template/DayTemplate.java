package com.example.activity_diary.entity.template;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "day_template",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_day_tpl_user_name", columnNames = {"user_id", "name"})
        },
        indexes = {
                @Index(name = "idx_day_tpl_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayTemplate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String name;

    @OneToMany(
            mappedBy = "dayTemplate",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("position ASC")
    @Builder.Default
    private List<TemplateEntryItem> items = new ArrayList<>();
}
