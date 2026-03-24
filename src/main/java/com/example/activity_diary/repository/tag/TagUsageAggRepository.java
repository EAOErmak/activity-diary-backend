package com.example.activity_diary.repository.tag;

import com.example.activity_diary.dto.analytics.TagUsageAggDto;
import com.example.activity_diary.entity.TagUsageAgg;
import com.example.activity_diary.entity.TagUsageAggId;
import com.example.activity_diary.entity.enums.TagUsageBucket;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TagUsageAggRepository extends JpaRepository<TagUsageAgg, TagUsageAggId> {

    @Query("""
        select new com.example.activity_diary.dto.analytics.TagUsageAggDto(
            a.id.tagId,
            t.name,
            a.id.bucket,
            a.id.bucketStart,
            a.usageCount,
            a.totalDurationMinutes
        )
        from TagUsageAgg a, Tag t
        where a.id.userId = :userId
          and a.id.bucket = :bucket
          and t.id = a.id.tagId
          and (:tagId is null or a.id.tagId = :tagId)
          and (:dateFrom is null or a.id.bucketStart >= :dateFrom)
          and (:dateTo is null or a.id.bucketStart <= :dateTo)
        order by a.id.bucketStart asc, t.name asc
        """)
    List<TagUsageAggDto> findUsage(
            @Param("userId") Long userId,
            @Param("bucket") TagUsageBucket bucket,
            @Param("tagId") Long tagId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

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
