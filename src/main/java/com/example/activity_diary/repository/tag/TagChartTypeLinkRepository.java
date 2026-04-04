package com.example.activity_diary.repository.tag;

import com.example.activity_diary.entity.diary.TagChartTypeLink;
import com.example.activity_diary.entity.enums.ChartType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagChartTypeLinkRepository extends JpaRepository<TagChartTypeLink, Long> {

    @Query("""
        select link
        from TagChartTypeLink link
        where link.tag.id = :tagId
        order by link.chartType asc
    """)
    List<TagChartTypeLink> findByTagId(Long tagId);

    boolean existsByTagIdAndChartType(Long tagId, ChartType chartType);
}
