package com.example.activity_diary.repository.template;

import com.example.activity_diary.entity.template.WeekTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeekTemplateRepository extends JpaRepository<WeekTemplate, Long> {
    boolean existsByUser_IdAndNameIgnoreCase(Long userId, String name);
    Optional<WeekTemplate> findByIdAndUser_Id(Long id, Long userId);
    Page<WeekTemplate> findAllByUser_Id(Long userId, Pageable pageable);
}
