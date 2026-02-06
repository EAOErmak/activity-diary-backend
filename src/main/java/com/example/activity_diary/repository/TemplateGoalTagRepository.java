package com.example.activity_diary.repository;

import com.example.activity_diary.entity.TemplateGoalTag;
import com.example.activity_diary.entity.TemplateGoalTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateGoalTagRepository extends JpaRepository<TemplateGoalTag, TemplateGoalTagId> {
    void deleteByTemplateId(Long templateId);
}
