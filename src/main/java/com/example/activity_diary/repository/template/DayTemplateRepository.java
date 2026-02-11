package com.example.activity_diary.repository.template;

import com.example.activity_diary.entity.template.DayTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DayTemplateRepository extends JpaRepository<DayTemplate, Long> {
    boolean existsByUser_IdAndNameIgnoreCase(Long userId, String name);
    Optional<DayTemplate> findByIdAndUser_Id(Long id, Long userId);
}
