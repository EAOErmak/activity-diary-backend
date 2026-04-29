package com.example.activity_diary.repository.diary;

import com.example.activity_diary.entity.diary.EntryMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ActivityItemRepository extends JpaRepository<EntryMetric, Long> {
    List<EntryMetric> findAllByDiaryEntryIdIn(List<Long> diaryIds);

    @Query("""
        select distinct metric
        from EntryMetric metric
        join fetch metric.metricType metricType
        left join fetch metric.values value
        left join fetch value.unit unit
        where metric.diaryEntry.id in :diaryEntryIds
        order by metric.diaryEntry.id, metric.id
    """)
    List<EntryMetric> findAllDetailedByDiaryEntryIdIn(@Param("diaryEntryIds") Collection<Long> diaryEntryIds);
}
