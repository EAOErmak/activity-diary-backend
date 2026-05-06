package com.example.activity_diary.repository.diary;

import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.dict.MetricNameUnitLink;
import com.example.activity_diary.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MetricNameUnitLinkRepository extends JpaRepository<MetricNameUnitLink, Long> {

    boolean existsByMetricNameIdAndMetricUnitId(Long metricNameId, Long metricUnitId);

    @Query(
            value = """
                select unit
                from MetricNameUnitLink link
                join link.metricUnit unit
                where link.metricName.id = :metricNameId
                order by lower(unit.label) asc, unit.id asc
            """,
            countQuery = """
                select count(unit.id)
                from MetricNameUnitLink link
                join link.metricUnit unit
                where link.metricName.id = :metricNameId
            """
    )
    Page<DictionaryItem> findUnitsPageByMetricNameId(Long metricNameId, Pageable pageable);

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

    @Query(
            value = """
                select unit
                from MetricNameUnitLink link
                join link.metricUnit unit
                where link.metricName.id = :metricNameId
                  and unit.active = true
                  and unit.type = com.example.activity_diary.entity.enums.DictionaryType.METRIC_UNIT
                  and (unit.allowedRole is null or unit.allowedRole = :role)
                order by lower(unit.label) asc, unit.id asc
            """,
            countQuery = """
                select count(unit.id)
                from MetricNameUnitLink link
                join link.metricUnit unit
                where link.metricName.id = :metricNameId
                  and unit.active = true
                  and unit.type = com.example.activity_diary.entity.enums.DictionaryType.METRIC_UNIT
                  and (unit.allowedRole is null or unit.allowedRole = :role)
            """
    )
    Page<DictionaryItem> findVisibleUnitsPageByMetricNameId(
            Long metricNameId,
            Role role,
            Pageable pageable
    );

    @Query(
            value = """
                select unit
                from MetricNameUnitLink link
                join link.metricUnit unit
                where link.metricName.id = :metricNameId
                  and unit.active = true
                  and unit.type = com.example.activity_diary.entity.enums.DictionaryType.METRIC_UNIT
                  and (unit.allowedRole is null or unit.allowedRole = :role)
                  and lower(unit.label) like concat('%', :query, '%')
                order by lower(unit.label) asc, unit.id asc
            """,
            countQuery = """
                select count(unit.id)
                from MetricNameUnitLink link
                join link.metricUnit unit
                where link.metricName.id = :metricNameId
                  and unit.active = true
                  and unit.type = com.example.activity_diary.entity.enums.DictionaryType.METRIC_UNIT
                  and (unit.allowedRole is null or unit.allowedRole = :role)
                  and lower(unit.label) like concat('%', :query, '%')
            """
    )
    Page<DictionaryItem> findVisibleUnitsPageByMetricNameIdAndLabelSearch(
            Long metricNameId,
            Role role,
            String query,
            Pageable pageable
    );
}
