// DayGoal.java
package com.example.activity_diary.entity.goal;

import com.example.activity_diary.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "day_goal",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_day_goal_week_day_index",
                        columnNames = {"week_goal_id", "day_index"}
                )
        },
        indexes = {
                @Index(name = "idx_day_goal_week", columnList = "week_goal_id"),
                @Index(name = "idx_day_goal_date", columnList = "target_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayGoal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "week_goal_id", nullable = false)
    private WeekGoal weekGoal;

    /**
     * 1..7 (например: Пн=1 ... Вс=7) — удобно для WeekTemplate.
     */
    @Column(name = "day_index", nullable = false)
    private Integer dayIndex;

    /**
     * Если ты привязываешь к конкретной дате — заполняй.
     */
    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "when_started", nullable = false)
    private Instant whenStarted;

    @Column(name = "when_ended", nullable = false)
    private Instant whenEnded;

    /** 0..100 */
    @Column(nullable = false)
    @Builder.Default
    private Integer completeness = 0;

    @OneToMany(
            mappedBy = "dayGoal",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("position ASC")
    @Builder.Default
    private List<DiaryEntryGoal> entryGoals = new ArrayList<>();

    public void attachTo(WeekGoal week) {
        this.weekGoal = week;
    }

    public void detach() {
        this.weekGoal = null;
    }

    public void setCompleteness(Integer completeness) {
        this.completeness = clampPct(completeness);
    }

    public void addEntryGoal(DiaryEntryGoal goal) {
        if (goal == null) throw new IllegalArgumentException("DiaryEntryGoal cannot be null");
        goal.attachTo(this);
        this.entryGoals.add(goal);
    }

    public void removeEntryGoal(DiaryEntryGoal goal) {
        if (goal == null) return;
        this.entryGoals.remove(goal);
        goal.detach();
    }

    private static final int MAX = 200;

    private static int clampPct(Integer v) {
        if (v == null) return 0;
        return Math.max(0, Math.min(MAX, v));
    }
}
