package com.example.activity_diary.entity.goal;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.base.BaseEntity;
import com.example.activity_diary.entity.diary.DiaryEntry;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "diary_entry_goal",
        indexes = {
                @Index(name = "idx_entry_goal_user", columnList = "user_id"),
                @Index(name = "idx_entry_goal_day", columnList = "day_goal_id"),
                @Index(name = "idx_entry_goal_range", columnList = "when_started, when_ended"),
                @Index(name = "idx_entry_goal_entry", columnList = "current_entry_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DiaryEntryGoal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_goal_id", nullable = false)
    private DayGoal dayGoal;

    /**
     * Позиция внутри DayGoal (если порядок важен, как в DayTemplate).
     */
    @Column(nullable = false)
    private Integer position;

    @Column(name = "when_started", nullable = false)
    private Instant whenStarted;

    @Column(name = "when_ended", nullable = false)
    private Instant whenEnded;

    /**
     * Ожидаемая длительность (минуты) для расчёта completeness по duration.
     */
    @Column(name = "expected_duration_min", nullable = false)
    private Integer expectedDurationMin;

    // --------- SNAPSHOT (из Template) ----------
    @Column(nullable = false, length = 120)
    private String name;

    private Short mood;

    @Column(length = 1000)
    private String description;

    // ---------- LINK TO ACTUAL ENTRY (последняя/актуальная запись) ----------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_entry_id")
    private DiaryEntry currentEntry;

    /** 0..100 */
    @Column(nullable = false)
    @Builder.Default
    private Integer completeness = 0;

    @OneToMany(
            mappedBy = "entryGoal",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<EntryMetricGoal> metricGoals = new HashSet<>();

    public void attachTo(DayGoal day) {
        this.dayGoal = day;
    }

    public void detach() {
        this.dayGoal = null;
    }

    public void setCompleteness(Integer completeness) {
        this.completeness = clampPct(completeness);
    }

    public void addMetricGoal(EntryMetricGoal metric) {
        if (metric == null) throw new IllegalArgumentException("MetricGoal cannot be null");
        metric.attachTo(this);
        this.metricGoals.add(metric);
    }

    public void removeMetricGoal(EntryMetricGoal metric) {
        if (metric == null) return;
        this.metricGoals.remove(metric);
        metric.detach();
    }

    private static final int MAX = 200;

    private static int clampPct(Integer v) {
        if (v == null) return 0;
        return Math.max(0, Math.min(MAX, v));
    }
}
