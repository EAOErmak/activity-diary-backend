package com.example.activity_diary.repository.goal;

import com.example.activity_diary.entity.goal.DiaryEntryGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiaryEntryGoalRepository extends JpaRepository<DiaryEntryGoal, Long> {
    Optional<DiaryEntryGoal> findByIdAndUser_Id(Long id, Long userId);
}
