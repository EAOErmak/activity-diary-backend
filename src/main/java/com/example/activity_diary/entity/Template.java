package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.enums.TemplateType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "template",
        uniqueConstraints = {
                // Имя уникально в рамках пользователя + типа (можно иметь "Рабочий" день и "Рабочая" неделя)
                @UniqueConstraint(name = "uk_template_user_type_name", columnNames = {"user_id", "type", "name"})
        },
        indexes = {
                @Index(name = "idx_template_user", columnList = "user_id"),
                @Index(name = "idx_template_type", columnList = "type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Template extends BaseEntity {

    /** Владелец шаблона */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** DAY или WEEK */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TemplateType type;

    /** Имя для удобства в UI */
    @Column(nullable = false, length = 120)
    private String name;

    /**
     * Список элементов для DAY-шаблона.
     * Заполняется только если type = DAY.
     */
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private java.util.List<TemplateEntryItem> dayItems = new java.util.ArrayList<>();

    /**
     * Список элементов для WEEK-шаблона.
     * Заполняется только если type = WEEK.
     */
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private java.util.List<TemplateDayItem> weekItems = new java.util.ArrayList<>();
}
