package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.enums.EntryStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "diary_entry",
        indexes = {
                @Index(name = "idx_diary_user", columnList = "user_id"),
                @Index(name = "idx_diary_started", columnList = "when_started"),
                @Index(name = "idx_diary_status", columnList = "status"),
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DiaryEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(
            mappedBy = "diaryEntry",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<EntryMetric> metrics = new ArrayList<>();

    @Column(name = "when_started", nullable = false)
    private Instant whenStarted;

    @Column(name = "when_ended", nullable = false)
    private Instant whenEnded;

    @Column(nullable = false)
    private Integer duration;

    private Short mood;

    @Column(length = 1000)
    private String description;

    @ManyToMany
    @JoinTable(
            name = "diary_entry_tag",
            joinColumns = @JoinColumn(name = "entry_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new java.util.HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryStatus status;

    public void updateDescription(String description) {
        String trimmed = description == null ? null : description.trim();
        if (trimmed == null || trimmed.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        this.description = trimmed;
    }

    public void updateMood(Short mood) {
        if (mood != null && (mood < 1 || mood > 5)) {
            throw new IllegalArgumentException("Mood must be between 1 and 5");
        }
        this.mood = mood;
    }

    public static DiaryEntry create(
            User user,
            Instant started,
            Instant ended,
            Short mood,
            String description
    ) {

        if (started == null || ended == null || !ended.isAfter(started)) {
            throw new IllegalArgumentException("Invalid time range");
        }

        int duration = (int) java.time.Duration
                .between(started, ended)
                .toMinutes();

        DiaryEntry entry = DiaryEntry.builder()
                .user(user)
                .whenStarted(started)
                .whenEnded(ended)
                .duration(duration)
                .mood(mood)
                .description(description)
                .status(EntryStatus.LOSE)
                .build();

        entry.updateMood(mood);
        entry.autoUpdateStatusByTime(Instant.now());

        return entry;
    }

    public void autoUpdateStatusByTime(Instant now) {
        if (this.status == EntryStatus.DELETED) return;

        if (whenEnded.isAfter(now)) {
            this.status = EntryStatus.LOSE;
        } else {
            this.status = EntryStatus.WIN;
        }
    }

    public void updateTime(Instant started, Instant ended) {
        if (this.whenEnded.isBefore(Instant.now())) {
            throw new IllegalStateException("Cannot modify entry after it has ended");
        }

        if (started == null || ended == null || !ended.isAfter(started)) {
            throw new IllegalArgumentException("Invalid time range");
        }

        this.whenStarted = started;
        this.whenEnded = ended;
        this.duration = (int) java.time.Duration
                .between(started, ended)
                .toMinutes();

        autoUpdateStatusByTime(Instant.now());
    }

    public void changeStatus(EntryStatus newStatus) {

        if (this.status == EntryStatus.DELETED) {
            throw new IllegalStateException("Deleted entry cannot change status");
        }

        if (this.status == EntryStatus.LOSE
                && newStatus == EntryStatus.WIN
                && this.whenEnded.isBefore(Instant.now())) {
            throw new IllegalStateException("Cannot change LOSE to WIN for past entry");
        }

        this.status = newStatus;
    }

    public void markDeleted() {
        this.status = EntryStatus.DELETED;
    }

    public void setTags(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("At least one tag is required");
        }
        if (tags.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Tags cannot contain null");
        }

        if (this.tags == null) this.tags = new java.util.HashSet<>();
        this.tags.clear();
        this.tags.addAll(tags);
    }

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

    public boolean isWin() {
        return this.status == EntryStatus.WIN;
    }

    public boolean isLose() {
        return this.status == EntryStatus.LOSE;
    }
}
