package com.example.activity_diary.repository.template;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateGoalMetricRepository extends JpaRepository<TemplateGoalMetric, TemplateGoalMetricId> {
    void deleteByTemplateId(Long templateId);
}
