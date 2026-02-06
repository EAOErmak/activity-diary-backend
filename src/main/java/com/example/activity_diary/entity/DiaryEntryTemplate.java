package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "diary_entry_template",
        uniqueConstraints = {
                // Имя шаблона уникально в рамках пользователя
                @UniqueConstraint(name = "uk_entry_tpl_user_name", columnNames = {"user_id", "name"})
        },
        indexes = {
                @Index(name = "idx_entry_tpl_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryEntryTemplate extends BaseEntity {

    /** Владелец шаблона */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Имя шаблона (для UI) */
    @Column(nullable = false, length = 120)
    private String name;

    /** Настроение (1..5 если ты так хочешь, можно валидировать в сервисе/методе) */
    private Short mood;

    /** Описание (можно хранить и как “текст с #тегами” если ты это используешь) */
    @Column(length = 1000)
    private String description;

    /**
     * Теги шаблона записи.
     * Важно: это шаблонные теги. При создании реальной DiaryEntry они копируются в запись.
     */
    @ManyToMany
    @JoinTable(
            name = "diary_entry_template_tag",
            joinColumns = @JoinColumn(name = "template_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    /**
     * Метрики шаблона (тип метрики -> значения по unit).
     * Это НЕ EntryMetric, потому что EntryMetric жёстко привязан к DiaryEntry.
     * Нужны отдельные сущности под шаблоны.
     */
    @OneToMany(
            mappedBy = "template",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<EntryTemplateMetric> metrics = new ArrayList<>();

    /* ---------- DOMAIN HELPERS ---------- */

    public void updateName(String name) {
        String v = name == null ? null : name.trim();
        if (v == null || v.isBlank()) throw new IllegalArgumentException("Template name is required");
        if (v.length() > 120) throw new IllegalArgumentException("Template name is too long");
        this.name = v;
    }

    public void updateDescription(String description) {
        this.description = (description == null) ? null : description.trim();
    }

    public void updateMood(Short mood) {
        if (mood != null && (mood < 1 || mood > 5)) {
            throw new IllegalArgumentException("Mood must be between 1 and 5");
        }
        this.mood = mood;
    }

    public void setTags(Set<Tag> tags) {
        if (this.tags == null) this.tags = new HashSet<>();
        this.tags.clear();
        if (tags != null) this.tags.addAll(tags);
    }

    /**
     * Добавить метрику-шаблон.
     * Важно: EntryTemplateMetric.create(...) прикрепляет её к этому шаблону.
     */
    public void addMetric(EntryTemplateMetric metric) {
        if (metric == null) throw new IllegalArgumentException("Metric cannot be null");
        metric.attachTo(this);
        this.metrics.add(metric);
    }

    public void removeMetric(EntryTemplateMetric metric) {
        if (metric == null) return;
        this.metrics.remove(metric);
        metric.detach();
    }

    /**
     * Удобная фабрика.
     * (Логику уникальности name в рамках user обеспечивает БД constraint.)
     */
    public static DiaryEntryTemplate create(User user, String name, Short mood, String description) {
        if (user == null) throw new IllegalArgumentException("User is required");
        DiaryEntryTemplate t = DiaryEntryTemplate.builder()
                .user(user)
                .name(name == null ? null : name.trim())
                .mood(mood)
                .description(description == null ? null : description.trim())
                .build();

        if (t.name == null || t.name.isBlank()) throw new IllegalArgumentException("Template name is required");
        if (t.name.length() > 120) throw new IllegalArgumentException("Template name is too long");
        if (mood != null && (mood < 1 || mood > 5)) throw new IllegalArgumentException("Mood must be between 1 and 5");

        return t;
    }
}

