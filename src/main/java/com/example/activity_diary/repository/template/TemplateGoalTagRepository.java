package com.example.activity_diary.repository.template;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.activity_diary.entity.template.TemplateGoalTag;
import com.example.activity_diary.entity.template.TemplateGoalTagId;

public interface TemplateGoalTagRepository extends JpaRepository<TemplateGoalTag, TemplateGoalTagId> {
    void deleteByTemplateId(Long templateId);
}
