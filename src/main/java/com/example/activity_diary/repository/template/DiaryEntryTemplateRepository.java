package com.example.activity_diary.repository.template;

import com.example.activity_diary.entity.DiaryEntryTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface DiaryEntryTemplateRepository extends JpaRepository<DiaryEntryTemplate, Long> {
    List<DiaryEntryTemplate> findAllByIdInAndUserId(Collection<Long> ids, Long userId);
    boolean existsByUser_IdAndNameIgnoreCase(Long userId, String name);

    @EntityGraph(attributePaths = {"tags", "metrics", "metrics.values"})
    Optional<DiaryEntryTemplate> findByIdAndUser_Id(Long id, Long userId);

    @EntityGraph(attributePaths = {"tags"})
    Page<DiaryEntryTemplate> findAllByUser_Id(Long userId, Pageable pageable);
}
