package com.example.activity_diary.repository.template;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.activity_diary.entity.template.TemplateGoalMetric;
import com.example.activity_diary.entity.template.TemplateGoalMetricId;

public interface TemplateGoalMetricRepository extends JpaRepository<TemplateGoalMetric, TemplateGoalMetricId> {
    void deleteByTemplateId(Long templateId);
}
