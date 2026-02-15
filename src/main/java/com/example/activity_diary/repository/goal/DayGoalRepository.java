package com.example.activity_diary.repository.goal;

import com.example.activity_diary.entity.goal.DayGoal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DayGoalRepository extends JpaRepository<DayGoal, Long> {
    Optional<DayGoal> findByWeekGoal_IdAndTargetDate(Long weekGoalId, LocalDate targetDate);

    Optional<DayGoal> findById(Long id);

    @EntityGraph(attributePaths = {})
    Optional<DayGoal> findSummaryByIdAndWeekGoal_User_Id(Long id, Long userId);

    @EntityGraph(attributePaths = {"entryGoals", "entryGoals.currentEntry"})
    Optional<DayGoal> findDetailByIdAndWeekGoal_User_Id(Long id, Long userId);

    @Query("""
        select d
        from DayGoal d
        where d.weekGoal.user.id = :userId
          and d.targetDate between :from and :to
        order by d.targetDate asc
    """)
    List<DayGoal> findAllByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
        select d
        from DayGoal d
        where d.weekGoal.user.id = :userId
          and d.weekGoal.id = :weekGoalId
        order by d.dayIndex asc
    """)
    List<DayGoal> findAllByUserAndWeek(
            @Param("userId") Long userId,
            @Param("weekGoalId") Long weekGoalId
    );

    long countByWeekGoal_Id(Long weekGoalId);
}
