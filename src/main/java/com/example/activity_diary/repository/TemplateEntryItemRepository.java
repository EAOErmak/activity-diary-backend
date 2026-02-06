package com.example.activity_diary.repository;

import com.example.activity_diary.entity.TemplateEntryItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TemplateEntryItemRepository extends JpaRepository<TemplateEntryItem, Long> {

    @Query("""
        select distinct di
        from TemplateEntryItem di
          join fetch di.entryTemplate et
          left join fetch et.tags
          left join fetch et.metrics m
          left join fetch m.values mv
        where di.template.id in :dayIds
        order by di.template.id, di.position
    """)
    List<TemplateEntryItem> findDayItemsGraph(@Param("dayIds") List<Long> dayIds);

    void deleteByTemplateId(Long templateId);
}
