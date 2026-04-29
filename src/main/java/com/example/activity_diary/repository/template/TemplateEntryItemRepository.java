package com.example.activity_diary.repository.template;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.example.activity_diary.entity.template.TemplateEntryItem;

import java.util.Collection;
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

    @Query("""
        select item
        from TemplateEntryItem item
        join fetch item.dayTemplate dayTemplate
        join fetch item.entryTemplate entryTemplate
        where item.dayTemplate.id in :dayTemplateIds
        order by item.dayTemplate.id asc, item.position asc
    """)
    List<TemplateEntryItem> findAllByDayTemplateIdInWithEntryTemplate(
            @Param("dayTemplateIds") Collection<Long> dayTemplateIds
    );
    
    void deleteByDayTemplate_Id(Long dayTemplateId);
}
