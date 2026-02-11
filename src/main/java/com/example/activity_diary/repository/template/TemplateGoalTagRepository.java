package com.example.activity_diary.repository.template;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateGoalTagRepository extends JpaRepository<TemplateGoalTag, TemplateGoalTagId> {
    void deleteByTemplateId(Long templateId);
}
