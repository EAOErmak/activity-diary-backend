package com.example.activity_diary.repository.template;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.activity_diary.entity.template.TemplateDayItem;

public interface TemplateDayItemRepository extends JpaRepository<TemplateDayItem, Long> {
    void deleteByTemplateId(Long templateId);
}
