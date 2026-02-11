package com.example.activity_diary.repository.template;

import com.example.activity_diary.entity.enums.TemplateType;
import com.example.activity_diary.entity.template.Template;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
        import org.springframework.data.repository.query.Param;

import java.util.*;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    Optional<Template> findByIdAndUserId(Long id, Long userId);

    Page<Template> findAllByUserId(Long userId, Pageable pageable);

    List<Template> findAllByIdInAndUserIdAndType(List<Long> ids, Long userId, TemplateType type);

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

    // ---- Type (cheap) ----
    @Query("select t.type from Template t where t.id = :id and t.user.id = :userId")
    Optional<TemplateType> findTypeByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    // ---- View graphs (no proxy in JSON because мы в DTO, но нужно чтобы mapper имел данные) ----
    @EntityGraph(attributePaths = {"dayItems", "dayItems.entryTemplate"})
    @Query("select t from Template t where t.id = :id and t.user.id = :userId")
    Optional<Template> findDayViewByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @EntityGraph(attributePaths = {"weekItems", "weekItems.dayTemplate"})
    @Query("select t from Template t where t.id = :id and t.user.id = :userId")
    Optional<Template> findWeekViewByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    // ---- Goals для view (отдельно) ----
    @Query("""
        select g from TemplateGoalTag g
        where g.template.id = :templateId and g.template.user.id = :userId
    """)
    List<TemplateGoalTag> findGoalTagsByTemplateId(@Param("templateId") Long templateId, @Param("userId") Long userId);

    @Query("""
        select g from TemplateGoalMetric g
        where g.template.id = :templateId and g.template.user.id = :userId
    """)
    List<TemplateGoalMetric> findGoalMetricsByTemplateId(@Param("templateId") Long templateId, @Param("userId") Long userId);

    // ---- Bulk counts for list ----
    @Query("""
        select i.template.id, count(i)
        from TemplateEntryItem i
        where i.template.id in :ids
        group by i.template.id
    """)
    List<Object[]> countDayItemsRaw(@Param("ids") List<Long> ids);

    @Query("""
        select i.template.id, count(i)
        from TemplateDayItem i
        where i.template.id in :ids
        group by i.template.id
    """)
    List<Object[]> countWeekItemsRaw(@Param("ids") List<Long> ids);

    @Query("""
        select g.template.id, count(g)
        from TemplateGoalTag g
        where g.template.id in :ids
        group by g.template.id
    """)
    List<Object[]> countGoalTagsRaw(@Param("ids") List<Long> ids);

    @Query("""
        select g.template.id, count(g)
        from TemplateGoalMetric g
        where g.template.id in :ids
        group by g.template.id
    """)
    List<Object[]> countGoalMetricsRaw(@Param("ids") List<Long> ids);

    // маленькие удобные адаптеры (default methods)
    default Map<Long, Integer> countDayItems(List<Long> ids) { return toCountMap(countDayItemsRaw(ids)); }
    default Map<Long, Integer> countWeekItems(List<Long> ids) { return toCountMap(countWeekItemsRaw(ids)); }
    default Map<Long, Integer> countGoalTags(List<Long> ids) { return toCountMap(countGoalTagsRaw(ids)); }
    default Map<Long, Integer> countGoalMetrics(List<Long> ids) { return toCountMap(countGoalMetricsRaw(ids)); }

    private static Map<Long, Integer> toCountMap(List<Object[]> rows) {
        Map<Long, Integer> m = new HashMap<>();
        if (rows == null) return m;
        for (Object[] r : rows) {
            if (r == null || r.length < 2) continue;
            Long id = (Long) r[0];
            Long c = (Long) r[1];
            m.put(id, c == null ? 0 : c.intValue());
        }
        return m;
    }
}
