package com.example.activity_diary.repository.diary;

import com.example.activity_diary.entity.MetricUsageAgg;
import com.example.activity_diary.entity.MetricUsageAggId;
import com.example.activity_diary.entity.enums.TagUsageBucket;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MetricUsageAggRepository extends JpaRepository<MetricUsageAgg, MetricUsageAggId> {

    @Query("""
        select
            a.id.metricTypeId as metricTypeId,
            metricType.label as metricTypeLabel,
            a.id.unitId as unitId,
            unit.label as unitLabel,
            a.id.bucket as bucket,
            a.id.bucketStart as bucketStart,
            a.valueSum as valueSum,
            a.valueCount as valueCount
        from MetricUsageAgg a, DictionaryItem metricType, DictionaryItem unit
        where a.id.userId = :userId
          and a.id.bucket = :bucket
          and metricType.id = a.id.metricTypeId
          and unit.id = a.id.unitId
          and (:metricTypeId is null or a.id.metricTypeId = :metricTypeId)
          and (:unitId is null or a.id.unitId = :unitId)
          and a.id.bucketStart >= coalesce(:dateFrom, a.id.bucketStart)
          and a.id.bucketStart <= coalesce(:dateTo, a.id.bucketStart)
        order by a.id.bucketStart asc, metricType.label asc, unit.label asc
        """)
    List<MetricUsageAggRow> findUsageRows(
            @Param("userId") Long userId,
            @Param("bucket") TagUsageBucket bucket,
            @Param("metricTypeId") Long metricTypeId,
            @Param("unitId") Long unitId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Modifying
    @Query(value = """
        INSERT INTO metric_usage_agg(
            user_id, metric_type_id, unit_id, bucket, bucket_start, value_sum, value_count
        )
        VALUES (
            :userId, :metricTypeId, :unitId, :bucket, :bucketStart, :sumInc, :countInc
        )
        ON CONFLICT (user_id, metric_type_id, unit_id, bucket, bucket_start)
        DO UPDATE SET
          value_sum = metric_usage_agg.value_sum + EXCLUDED.value_sum,
          value_count = metric_usage_agg.value_count + EXCLUDED.value_count
        """, nativeQuery = true)
    void addDelta(
            @Param("userId") Long userId,
            @Param("metricTypeId") Long metricTypeId,
            @Param("unitId") Long unitId,
            @Param("bucket") String bucket,
            @Param("bucketStart") LocalDate bucketStart,
            @Param("sumInc") long sumInc,
            @Param("countInc") int countInc
    );
}
