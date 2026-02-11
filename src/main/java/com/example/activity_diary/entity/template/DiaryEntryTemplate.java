package com.example.activity_diary.entity.template;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.diary.Tag;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String name;

    private Short mood;

    @Column(length = 1000)
    private String description;

    @ManyToMany
    @JoinTable(
            name = "diary_entry_template_tag",
            joinColumns = @JoinColumn(name = "template_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(
            mappedBy = "template",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<EntryTemplateMetric> metrics = new HashSet<>();

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

