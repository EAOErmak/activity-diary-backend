package com.example.activity_diary.repository.goal;

import com.example.activity_diary.entity.goal.DayGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DayGoalRepository extends JpaRepository<DayGoal, Long> {
    Optional<DayGoal> findByWeekGoal_IdAndTargetDate(Long weekGoalId, LocalDate targetDate);
}
