package com.example.activity_diary.repository.template;

import com.example.activity_diary.entity.TemplateDayItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateDayItemRepository extends JpaRepository<TemplateDayItem, Long> {
    void deleteByTemplateId(Long templateId);
}
