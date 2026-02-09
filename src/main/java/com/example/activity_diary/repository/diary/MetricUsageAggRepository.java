package com.example.activity_diary.repository.diary;

import com.example.activity_diary.entity.MetricUsageAgg;
import com.example.activity_diary.entity.MetricUsageAggId;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface MetricUsageAggRepository extends JpaRepository<MetricUsageAgg, MetricUsageAggId> {

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
