package com.example.activity_diary.repository.template;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.example.activity_diary.entity.template.TemplateEntryItem;

import java.util.List;

public interface TemplateEntryItemRepository extends JpaRepository<TemplateEntryItem, Long> {

    @Query("""
        select distinct di
        from TemplateEntryItem di
          join fetch di.entryTemplate et
          left join fetch et.metrics m
          left join fetch m.values mv
        where di.dayTemplate.id in :dayIds
        order by di.dayTemplate.id, di.position
    """)
    List<TemplateEntryItem> findDayItemsGraph(@Param("dayIds") List<Long> dayIds);
    
    void deleteByDayTemplate_Id(Long dayTemplateId);
}
