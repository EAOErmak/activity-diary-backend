package com.example.activity_diary.repository.template;

import com.example.activity_diary.entity.template.EntryTemplateMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EntryTemplateMetricRepository extends JpaRepository<EntryTemplateMetric, Long> {

    @Query("""
        select distinct metric
        from EntryTemplateMetric metric
        join fetch metric.template template
        join fetch metric.metricType metricType
        left join fetch metric.values value
        left join fetch value.unit unit
        where template.id in :templateIds
        order by template.id asc, metric.id asc
    """)
    List<EntryTemplateMetric> findAllDetailedByTemplateIdIn(@Param("templateIds") Collection<Long> templateIds);
}
