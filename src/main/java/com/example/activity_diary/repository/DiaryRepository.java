package com.example.activity_diary.repository;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.enums.EntryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface DiaryRepository extends JpaRepository<DiaryEntry, Long> {

    // page-based query (standard)
    Page<DiaryEntry> findByUserId(Long userId, Pageable pageable);

    // existing list-based API (used by some internal flows) — fetch joins to avoid N+1
    @Query("""
        select distinct d from DiaryEntry d
        left join fetch d.what
        left join fetch d.whatHappened
        left join fetch d.whatDidYouDo items
        left join fetch items.name
        left join fetch items.unit
        where d.user.id = :userId
    """)
    List<DiaryEntry> findFullByUserId(@Param("userId") Long userId);

    List<DiaryEntry> findByStatus(EntryStatus status);

    List<DiaryEntry> findByUserIdAndStatus(Long userId, EntryStatus status);

    List<DiaryEntry> findAllByUserIdAndWhatHappenedIdAndWhenStartedBetween(Long userId, Long whatHappenedId, LocalDateTime localDateTime, LocalDateTime localDateTime1);

    @Query("""
        select distinct d from DiaryEntry d
        left join fetch d.what
        left join fetch d.whatHappened
        left join fetch d.whatDidYouDo items
        left join fetch items.name
        left join fetch items.unit
        where d.user.id = :userId
          and d.whatHappened.id = :whatHappenedId
          and d.whenStarted between :from and :to
    """)
    List<DiaryEntry> findFullForAnalytics(
            @Param("userId") Long userId,
            @Param("whatHappenedId") Long whatHappenedId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        select distinct d from DiaryEntry d
        left join fetch d.what
        left join fetch d.whatHappened
        left join fetch d.whatDidYouDo items
        left join fetch items.name
        left join fetch items.unit
        where d.user.id = :userId
          and d.whatHappened.id = :whatHappenedId
          and d.whenStarted between :from and :to
    """)
    List<DiaryEntry> findForAnalyticsByCategory(
            @Param("userId") Long userId,
            @Param("whatHappenedId") Long whatHappenedId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        select distinct d from DiaryEntry d
        left join fetch d.what
        left join fetch d.whatHappened
        left join fetch d.whatDidYouDo items
        left join fetch items.name
        left join fetch items.unit
        where d.user.id = :userId
          and d.what.id = :whatId
          and d.whenStarted between :from and :to
    """)

    List<DiaryEntry> findForAnalyticsByWhat(
            @Param("userId") Long userId,
            @Param("whatId") Long whatId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT COUNT(d)
        FROM DiaryEntry d
        WHERE d.createdAt >= :start AND d.createdAt < :end
    """)
    long countEntriesBetween(
            @Param("start") Instant start,
            @Param("end") Instant end
    );


    @Query("""
        SELECT COUNT(DISTINCT d.user.id)
        FROM DiaryEntry d
        WHERE d.createdAt >= :start AND d.createdAt < :end
    """)
    long countActiveUsersBetween(
            @Param("start") Instant start,
            @Param("end") Instant  end
    );
}
