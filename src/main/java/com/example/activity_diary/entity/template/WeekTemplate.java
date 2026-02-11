package com.example.activity_diary.entity.template;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "week_template",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_week_tpl_user_name", columnNames = {"user_id", "name"})
        },
        indexes = {
                @Index(name = "idx_week_tpl_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeekTemplate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String name;

    @OneToMany(
            mappedBy = "weekTemplate",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("dayOfWeek ASC")
    @Builder.Default
    private List<TemplateDayItem> items = new ArrayList<>();
}
