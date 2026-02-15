package com.example.activity_diary.repository.goal;

import com.example.activity_diary.entity.goal.WeekGoal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
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

    @EntityGraph(attributePaths = {})
    Optional<WeekGoal> findSummaryByIdAndUser_Id(Long id, Long userId);

    @EntityGraph(attributePaths = {
            "days",
            "days.entryGoals",
            "days.entryGoals.currentEntry"
    })
    Optional<WeekGoal> findDetailByIdAndUser_Id(Long id, Long userId);

    @Query("""
        select w
        from WeekGoal w
        where w.user.id = :userId
          and w.whenStarted <= :to
          and w.whenEnded   >= :from
        order by w.whenStarted asc
    """)
    List<WeekGoal> findByUserIdAndRange(
            @Param("userId") Long userId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
