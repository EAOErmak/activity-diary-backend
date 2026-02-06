package com.example.activity_diary.repository;

import com.example.activity_diary.entity.Template;
import com.example.activity_diary.entity.enums.TemplateType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    Optional<Template> findByIdAndUserId(Long id, Long userId);

    List<Template> findAllByIdInAndUserIdAndType(Collection<Long> ids, Long userId, TemplateType type);

    @Query("""
        select distinct w
        from Template w
          left join fetch w.weekItems wi
          left join fetch wi.dayTemplate d
        where w.id = :weekId
          and w.user.id = :userId
          and w.type = com.example.activity_diary.entity.enums.TemplateType.WEEK
    """)
    Optional<Template> findWeekWithDays(@Param("weekId") Long weekId, @Param("userId") Long userId);

    @Query("""
        select distinct d
        from Template d
          left join fetch d.dayItems di
          left join fetch di.entryTemplate et
        where d.id = :dayId
          and d.user.id = :userId
          and d.type = com.example.activity_diary.entity.enums.TemplateType.DAY
    """)
    Optional<Template> findDayWithItemsBasic(@Param("dayId") Long dayId, @Param("userId") Long userId);
}
