package com.example.activity_diary.repository.goal;

import com.example.activity_diary.entity.goal.DiaryEntryGoal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DiaryEntryGoalRepository extends JpaRepository<DiaryEntryGoal, Long> {
    Optional<DiaryEntryGoal> findByIdAndUser_Id(Long id, Long userId);

    @EntityGraph(attributePaths = {"currentEntry"})
    Optional<DiaryEntryGoal> findSummaryByIdAndUser_Id(Long id, Long userId);

    @EntityGraph(attributePaths = {
            "currentEntry",
            "metricGoals",
            "metricGoals.metricType",
            "metricGoals.values",
            "metricGoals.values.unit"
    })
    Optional<DiaryEntryGoal> findDetailByIdAndUser_Id(Long id, Long userId);

    @Query("""
        select distinct g
        from DiaryEntryGoal g
        left join fetch g.currentEntry currentEntry
        left join fetch g.metricGoals metricGoal
        left join fetch metricGoal.metricType metricType
        left join fetch metricGoal.values value
        left join fetch value.unit unit
        where g.id in :goalIds
    """)
    List<DiaryEntryGoal> findAllMetricDetailsByIdIn(@Param("goalIds") Collection<Long> goalIds);

    @Query("""
        select g
        from DiaryEntryGoal g
        where g.user.id = :userId
          and g.dayGoal.targetDate = :date
        order by g.position asc
    """)
    List<DiaryEntryGoal> findAllByUserAndTargetDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

    @Query("""
        select g
        from DiaryEntryGoal g
        where g.user.id = :userId
          and g.dayGoal.id = :dayGoalId
        order by g.position asc
    """)
    List<DiaryEntryGoal> findAllByUserAndDayGoal(
            @Param("userId") Long userId,
            @Param("dayGoalId") Long dayGoalId
    );

    long countByDayGoal_Id(Long dayGoalId);
}
