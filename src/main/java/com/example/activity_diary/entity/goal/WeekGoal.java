// WeekGoal.java
package com.example.activity_diary.entity.goal;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "week_goal",
        indexes = {
                @Index(name = "idx_week_goal_user", columnList = "user_id"),
                @Index(name = "idx_week_goal_range", columnList = "when_started, when_ended")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class WeekGoal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "when_started", nullable = false)
    private Instant whenStarted;

    @Column(name = "when_ended", nullable = false)
    private Instant whenEnded;

    @Column(nullable = false)
    @Builder.Default
    private Integer completeness = 0;

    @OneToMany(
            mappedBy = "weekGoal",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("dayIndex ASC")
    @Builder.Default
    private List<DayGoal> days = new ArrayList<>();

    public void setCompleteness(Integer completeness) {
        this.completeness = clampPct(completeness);
    }

    public void addDay(DayGoal day) {
        if (day == null) throw new IllegalArgumentException("DayGoal cannot be null");
        day.attachTo(this);
        this.days.add(day);
    }

    public void removeDay(DayGoal day) {
        if (day == null) return;
        this.days.remove(day);
        day.detach();
    }

    private static final int MAX = 200;

    private static int clampPct(Integer v) {
        if (v == null) return 0;
        return Math.max(0, Math.min(MAX, v));
    }
}
