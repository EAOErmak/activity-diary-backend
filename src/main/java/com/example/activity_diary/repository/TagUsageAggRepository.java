package com.example.activity_diary.repository;

import com.example.activity_diary.entity.TagUsageAgg;
import com.example.activity_diary.entity.TagUsageAggId;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TagUsageAggRepository extends JpaRepository<TagUsageAgg, TagUsageAggId> {

    @Modifying
    @Query(value = """
        INSERT INTO tag_usage_agg(user_id, tag_id, bucket, bucket_start, usage_count, total_duration_minutes)
        VALUES (:userId, :tagId, :bucket, :bucketStart, :countInc, :durationInc)
        ON CONFLICT (user_id, tag_id, bucket, bucket_start)
        DO UPDATE SET
          usage_count = tag_usage_agg.usage_count + EXCLUDED.usage_count,
          total_duration_minutes = tag_usage_agg.total_duration_minutes + EXCLUDED.total_duration_minutes
        """, nativeQuery = true)
    void addDelta(
            @Param("userId") Long userId,
            @Param("tagId") Long tagId,
            @Param("bucket") String bucket,
            @Param("bucketStart") LocalDate bucketStart,
            @Param("countInc") int countInc,
            @Param("durationInc") long durationInc
    );
}
