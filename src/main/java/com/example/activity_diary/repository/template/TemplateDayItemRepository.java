package com.example.activity_diary.repository.template;

import com.example.activity_diary.entity.template.TemplateDayItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TemplateDayItemRepository extends JpaRepository<TemplateDayItem, Long> {
    @Query("""
        select item
        from TemplateDayItem item
        join fetch item.weekTemplate weekTemplate
        join fetch item.dayTemplate dayTemplate
        where item.weekTemplate.id in :weekTemplateIds
        order by item.weekTemplate.id asc, item.dayOfWeek asc
    """)
    List<TemplateDayItem> findAllByWeekTemplateIdInWithDayTemplate(
            @Param("weekTemplateIds") Collection<Long> weekTemplateIds
    );

    void deleteByWeekTemplate_Id(Long weekTemplateId);
}
