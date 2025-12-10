package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "diary_entry",
        indexes = {
                @Index(name = "idx_diary_user", columnList = "user_id"),
                @Index(name = "idx_diary_started", columnList = "when_started"),
                @Index(name = "idx_diary_status", columnList = "status"),
                @Index(name = "idx_diary_category", columnList = "category_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DiaryEntry extends BaseEntity {

    // ============================================================
    // RELATIONS
    // ============================================================

    // ✅ Category
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private DictionaryItem category;

    // ✅ Subcategory
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "sub_category_id", nullable = true)
    private DictionaryItem subCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // ✅ Metrics (бывший whatDidYouDo)
    @OneToMany(
            mappedBy = "diaryEntry",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<EntryMetric> metrics = new ArrayList<>();

    // ============================================================
    // TIME
    // ============================================================

    @Column(name = "when_started", nullable = false)
    private LocalDateTime whenStarted;

    @Column(name = "when_ended", nullable = false)
    private LocalDateTime whenEnded;

    @Column(nullable = false)
    private Integer duration;

    // ============================================================
    // META
    // ============================================================

    // ✅ Mood
    private Short mood;

    // ✅ Description
    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryStatus status;

    // ============================================================
    // UPDATE METHODS
    // ============================================================

    public void updateDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public void updateMood(Short mood) {
        if (mood != null && (mood < 1 || mood > 5)) {
            throw new IllegalArgumentException("Mood must be between 1 and 5");
        }
        this.mood = mood;
    }

    public void changeCategory(DictionaryItem newCategory) {
        this.category = newCategory;
    }

    public void changeSubCategory(DictionaryItem newSubCategory) {
        if (newSubCategory == null) {
            throw new IllegalArgumentException("SubCategory cannot be null");
        }
        this.subCategory = newSubCategory;
    }

    // ============================================================
    // FACTORY
    // ============================================================

    public static DiaryEntry create(
            User user,
            DictionaryItem category,
            DictionaryItem subCategory,
            LocalDateTime started,
            LocalDateTime ended,
            Short mood,
            String description
    ) {

        if (started == null || ended == null || !ended.isAfter(started)) {
            throw new IllegalArgumentException("Invalid time range");
        }

        int duration = (int) java.time.Duration
                .between(started, ended)
                .toMinutes();

        return DiaryEntry.builder()
                .user(user)
                .category(category)
                .subCategory(subCategory)
                .whenStarted(started)
                .whenEnded(ended)
                .duration(duration)
                .mood(mood)
                .description(description)
                .status(EntryStatus.DRAFT)
                .build();
    }

    // ============================================================
    // BUSINESS METHODS
    // ============================================================

    public void updateTime(LocalDateTime started, LocalDateTime ended) {

        if (started == null || ended == null || !ended.isAfter(started)) {
            throw new IllegalArgumentException("Invalid time range");
        }

        this.whenStarted = started;
        this.whenEnded = ended;
        this.duration = (int) java.time.Duration
                .between(started, ended)
                .toMinutes();
    }

    public void changeStatus(EntryStatus newStatus) {

        if (this.status == EntryStatus.DELETED) {
            throw new IllegalStateException("Deleted entry cannot change status");
        }

        this.status = newStatus;
    }

    public void markDeleted() {
        this.status = EntryStatus.DELETED;
    }

    // ✅ работы с метриками

    public void addMetric(EntryMetric item) {
        if (item == null) {
            throw new IllegalArgumentException("Metric cannot be null");
        }

        item.attachTo(this);
        this.metrics.add(item);
    }

    public void removeMetric(EntryMetric item) {
        if (item == null) return;

        this.metrics.remove(item);
        item.detach();
    }

    public boolean isDraft() {
        return this.status == EntryStatus.DRAFT;
    }

    public boolean isFinal() {
        return this.status == EntryStatus.FINAL;
    }
}
