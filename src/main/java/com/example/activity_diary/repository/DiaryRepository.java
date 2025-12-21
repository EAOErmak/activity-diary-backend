package com.example.activity_diary.repository;

import com.example.activity_diary.dto.diary.DiaryEntryViewDto;
import com.example.activity_diary.entity.DiaryEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface DiaryRepository extends JpaRepository<DiaryEntry, Long> {

    // ============================================================
    // BASE (ONLY USER DATA)
    // ============================================================

    @Query("""
        select new com.example.activity_diary.dto.diary.DiaryEntryViewDto(
            d.id,
            d.status,
            d.whenStarted,
            d.whenEnded,
            c.label,
            sc.label
        )
        from DiaryEntry d
        left join d.category c
        left join d.subCategory sc
        where d.user.id = :userId
          and d.status <> 'DELETED'
    """)
    Slice<DiaryEntryViewDto> findListByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
        select d
        from DiaryEntry d
        left join fetch d.category
        left join fetch d.subCategory
        where d.user.id = :userId
          and d.whenStarted < :to
          and d.whenEnded   > :from
        order by d.whenStarted asc
    """)
    List<DiaryEntry> findByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        select new com.example.activity_diary.dto.diary.DiaryEntryViewDto(
            d.id,
            d.status,
            d.whenStarted,
            d.whenEnded,
            c.label,
            sc.label
        )
        from DiaryEntry d
        left join d.category c
        left join d.subCategory sc
        where d.user.id = :userId
        order by d.updatedAt desc
    """)
    List<DiaryEntryViewDto> findAllByUserId(@Param("userId") Long userId);

    // ============================================================
    // FULL FETCH (NO N+1)
    // ============================================================

    @Query("""
        select distinct d from DiaryEntry d
        left join fetch d.category
        left join fetch d.subCategory
        left join fetch d.metrics items
        left join fetch items.metricType
        left join fetch items.unit
        where d.user.id = :userId
          and d.status <> 'DELETED'
    """)
    List<DiaryEntry> findFullByUserId(@Param("userId") Long userId);

    // ============================================================
    // ANALYTICS (SAFE + OPTIMIZED)
    // ============================================================

    @Query("""
        select distinct d from DiaryEntry d
        left join fetch d.subCategory
        left join fetch d.category
        left join fetch d.metrics items
        left join fetch items.metricType
        left join fetch items.unit
        where d.user.id = :userId
          and d.category.id = :categoryId
          and d.whenStarted between :from and :to
    """)
    List<DiaryEntry> findForAnalyticsByCategory(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        select distinct d from DiaryEntry d
        left join fetch d.subCategory
        left join fetch d.category
        left join fetch d.metrics items
        left join fetch items.metricType
        left join fetch items.unit
        where d.user.id = :userId
          and d.subCategory.id = :subCategoryId
          and d.whenStarted between :from and :to
    """)
    List<DiaryEntry> findForAnalyticsBySubCategory(
            @Param("userId") Long userId,
            @Param("subCategoryId") Long subCategoryId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // ============================================================
    // ADMIN / STATS (SAFE)
    // ============================================================

    @Query("""
        SELECT COUNT(d)
        FROM DiaryEntry d
        WHERE d.createdAt >= :start
          AND d.createdAt < :end
    """)
    long countEntriesBetween(
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("""
        SELECT COUNT(DISTINCT d.user.id)
        FROM DiaryEntry d
        WHERE d.createdAt >= :start
          AND d.createdAt < :end
    """)
    long countActiveUsersBetween(
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}
