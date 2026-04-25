package com.example.activity_diary.entity.template;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;

@Entity
@Table(
        name = "diary_entry_template",
        uniqueConstraints = {
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

    @Column(name = "time_start", columnDefinition = "TIME")
    private LocalTime timeStart;

    @Column(name = "time_end", columnDefinition = "TIME")
    private LocalTime timeEnd;

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

    public void updateTime(LocalTime start, LocalTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        this.timeStart = start;
        this.timeEnd = end;
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

    public static DiaryEntryTemplate create(
            User user,
            String name,
            Short mood,
            String description,
            LocalTime timeStart,
            LocalTime timeEnd
    ) {
        if (user == null) throw new IllegalArgumentException("User is required");

        String n = (name == null) ? null : name.trim();
        String d = (description == null) ? null : description.trim();

        if (n == null || n.isBlank()) throw new IllegalArgumentException("Template name is required");
        if (n.length() > 120) throw new IllegalArgumentException("Template name is too long");
        if (mood != null && (mood < 1 || mood > 5)) throw new IllegalArgumentException("Mood must be between 1 and 5");
        if (timeStart != null && timeEnd != null && timeEnd.isBefore(timeStart)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }

        return DiaryEntryTemplate.builder()
                .user(user)
                .name(n)
                .mood(mood)
                .description(d)
                .timeStart(timeStart)
                .timeEnd(timeEnd)
                .build();
    }
}

