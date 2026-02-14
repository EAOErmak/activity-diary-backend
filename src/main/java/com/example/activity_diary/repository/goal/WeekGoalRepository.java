package com.example.activity_diary.repository.goal;

import com.example.activity_diary.entity.goal.WeekGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeekGoalRepository extends JpaRepository<WeekGoal, Long> {
    Optional<WeekGoal> findByIdAndUser_Id(Long id, Long userId);
}
