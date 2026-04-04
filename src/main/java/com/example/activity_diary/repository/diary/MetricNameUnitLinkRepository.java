package com.example.activity_diary.repository.diary;

import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.dict.MetricNameUnitLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MetricNameUnitLinkRepository extends JpaRepository<MetricNameUnitLink, Long> {

    boolean existsByMetricNameIdAndMetricUnitId(Long metricNameId, Long metricUnitId);

    @Query("""
        select link
        from MetricNameUnitLink link
        join fetch link.metricUnit unit
        where link.metricName.id = :metricNameId
        order by unit.label asc
    """)
    List<MetricNameUnitLink> findByMetricNameId(Long metricNameId);

    void deleteByMetricNameIdAndMetricUnitId(Long metricNameId, Long metricUnitId);

    @Query("""
        select unit
        from MetricNameUnitLink link
        join link.metricUnit unit
        where link.metricName.id = :metricNameId
          and unit.active = true
          and unit.type = com.example.activity_diary.entity.enums.DictionaryType.METRIC_UNIT
        order by unit.label asc
    """)
    List<DictionaryItem> findUnitsByMetricNameId(Long metricNameId);
}
