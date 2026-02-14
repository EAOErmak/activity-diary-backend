package com.example.activity_diary.repository.goal;

import com.example.activity_diary.entity.goal.WeekGoal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface WeekGoalRepository extends JpaRepository<WeekGoal, Long> {
    Optional<WeekGoal> findByUser_IdAndWhenStarted(Long userId, Instant whenStarted);

    @EntityGraph(attributePaths = {
            "days",
            "days.entryGoals",
            "days.entryGoals.metricGoals",
            "days.entryGoals.metricGoals.values"
    })
    Optional<WeekGoal> findGraphByUser_IdAndWhenStarted(Long userId, Instant whenStarted);
}
