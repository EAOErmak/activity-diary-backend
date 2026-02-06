package com.example.activity_diary.repository;

import com.example.activity_diary.entity.DiaryEntryTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface DiaryEntryTemplateRepository extends JpaRepository<DiaryEntryTemplate, Long> {
    List<DiaryEntryTemplate> findAllByIdInAndUserId(Collection<Long> ids, Long userId);
}
