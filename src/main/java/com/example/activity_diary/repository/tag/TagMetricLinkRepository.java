package com.example.activity_diary.repository.tag;

import com.example.activity_diary.entity.diary.TagMetricLink;
import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.Role;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TagMetricLinkRepository extends JpaRepository<TagMetricLink, Long> {

    @Query("""
        select link
        from TagMetricLink link
        join fetch link.metricName metric
        where link.tag.id = :tagId
        order by metric.label asc
    """)
    List<TagMetricLink> findByTagId(Long tagId);

    @Query("""
        select metric
        from TagMetricLink link
        join link.metricName metric
        where link.tag.id = :tagId
          and metric.active = true
          and metric.type = com.example.activity_diary.entity.enums.DictionaryType.METRIC_NAME
          and (metric.allowedRole is null or metric.allowedRole = :role)
        order by metric.label asc
    """)
    List<DictionaryItem> findVisibleMetricNamesByTagId(Long tagId, Role role);

    @Query("""
        select distinct metric
        from TagMetricLink link
        join link.metricName metric
        where link.tag.id in :tagIds
          and metric.active = true
          and metric.type = com.example.activity_diary.entity.enums.DictionaryType.METRIC_NAME
          and (metric.allowedRole is null or metric.allowedRole = :role)
        order by metric.label asc
    """)
    List<DictionaryItem> findVisibleMetricNamesByTagIds(Collection<Long> tagIds, Role role);

    @Query("""
        select distinct metric.id
        from TagMetricLink link
        join link.metricName metric
        where link.tag.id in :tagIds
          and metric.type = com.example.activity_diary.entity.enums.DictionaryType.METRIC_NAME
    """)
    Set<Long> findMetricNameIdsByTagIds(Collection<Long> tagIds);

    boolean existsByTagIdAndMetricNameId(Long tagId, Long metricNameId);

    boolean existsByTagId(Long tagId);

    @Modifying
    @Query("""
        delete from TagMetricLink link
        where link.tag.id = :tagId
          and link.metricName.id = :metricNameId
    """)
    void deleteByTagIdAndMetricNameId(@Param("tagId") Long tagId, @Param("metricNameId") Long metricNameId);

    @Modifying
    @Query("""
        delete from TagMetricLink link
        where link.tag.id = :tagId
    """)
    void deleteByTagId(@Param("tagId") Long tagId);
}
