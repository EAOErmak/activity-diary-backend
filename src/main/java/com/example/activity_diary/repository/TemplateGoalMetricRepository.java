package com.example.activity_diary.repository;

import com.example.activity_diary.entity.TemplateGoalMetric;
import com.example.activity_diary.entity.TemplateGoalMetricId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateGoalMetricRepository extends JpaRepository<TemplateGoalMetric, TemplateGoalMetricId> {
    void deleteByTemplateId(Long templateId);
}
