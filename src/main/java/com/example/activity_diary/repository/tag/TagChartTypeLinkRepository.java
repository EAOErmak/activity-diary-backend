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
    """)
    List<TagChartTypeLink> findByTagId(Long tagId);

    @Query("""
        select link.chartType
        from TagChartTypeLink link
        where link.tag.id = :tagId
    """)
    List<ChartType> findChartTypesByTagId(Long tagId);

    boolean existsByTagIdAndChartType(Long tagId, ChartType chartType);

    void deleteByTagIdAndChartType(Long tagId, ChartType chartType);
}
