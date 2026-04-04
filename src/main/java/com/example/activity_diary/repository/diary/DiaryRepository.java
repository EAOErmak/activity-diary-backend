package com.example.activity_diary.repository.diary;

import com.example.activity_diary.dto.diary.DiaryEntryViewDto;
import com.example.activity_diary.entity.diary.DiaryEntry;

import com.example.activity_diary.entity.enums.EntryStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface DiaryRepository extends JpaRepository<DiaryEntry, Long> {

    @EntityGraph(attributePaths = {
            "user",
            "metrics",
            "metrics.metricType",
    })
    Optional<DiaryEntry> findGraphByIdAndUser_Id(Long id, Long userId);

    List<DiaryEntry> findAllByUserIdAndTags_Id(Long userId, Long tagId);

    @Query("""
        select distinct d
        from DiaryEntry d
        join d.tags t
        where d.user.id = :userId
          and t.id = :tagId
          and (:dateFrom is null or d.whenStarted >= :dateFrom)
          and (:dateTo is null or d.whenStarted <= :dateTo)
    """)
    List<DiaryEntry> findAllByUserIdAndTagIdAndWhenStartedRange(
            @Param("userId") Long userId,
            @Param("tagId") Long tagId,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo
    );

    List<DiaryEntry> findAllByUserIdAndWhenStartedBetweenAndStatusNotOrderByWhenStartedAsc(
            Long userId,
            Instant from,
            Instant to,
            EntryStatus status
    );

    Optional<DiaryEntry> findByIdAndUserId(Long id, Long userId);

    @Query("""
        select distinct d.user.id
        from DiaryEntry d
        where d.status = :currentStatus
          and d.whenStarted <= :now
          and d.whenEnded >= :now
    """)
    List<Long> findDistinctUserIdsToActivate(
            @Param("currentStatus") EntryStatus currentStatus,
            @Param("now") Instant now
    );

    @Modifying
    @Query("""
        update DiaryEntry d
        set d.status = :newStatus
        where d.status = :currentStatus
          and d.whenStarted <= :now
          and d.whenEnded >= :now
    """)
    int activateScheduledEntries(
            @Param("currentStatus") EntryStatus currentStatus,
            @Param("newStatus") EntryStatus newStatus,
            @Param("now") Instant now
    );

    @Query("""
        select distinct d.user.id
        from DiaryEntry d
        where d.status in :currentStatuses
          and d.whenEnded < :now
    """)
    List<Long> findDistinctUserIdsToFinish(
            @Param("currentStatuses") List<EntryStatus> currentStatuses,
            @Param("now") Instant now
    );

    @Modifying
    @Query("""
        update DiaryEntry d
        set d.status = :newStatus
        where d.status in :currentStatuses
          and d.whenEnded < :now
    """)
    int finishExpiredEntries(
            @Param("currentStatuses") List<EntryStatus> currentStatuses,
            @Param("newStatus") EntryStatus newStatus,
            @Param("now") Instant now
    );

    @Query("""
        select new com.example.activity_diary.dto.diary.DiaryEntryViewDto(
            d.id,
            d.status,
            d.whenStarted,
            d.whenEnded,
            (
                select t.name
                from DiaryEntry d2
                join d2.tags t
                where d2.id = d.id
                  and t.id = (
                      select min(t2.id)
                      from DiaryEntry d3
                      join d3.tags t2
                      where d3.id = d.id
                  )
            )
        )
        from DiaryEntry d
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
            (
                select t.name
                from DiaryEntry d2
                join d2.tags t
                where d2.id = d.id
                  and t.id = (
                      select min(t2.id)
                      from DiaryEntry d3
                      join d3.tags t2
                      where d3.id = d.id
                  )
            )
        )
        from DiaryEntry d
        where d.user.id = :userId
        order by d.updatedAt desc
    """)
    List<DiaryEntryViewDto> findAllByUserId(@Param("userId") Long userId);


    @Query("""
        select new com.example.activity_diary.dto.diary.DiaryEntryViewDto(
            d.id, d.status, d.whenStarted, d.whenEnded,
            coalesce(
                (
                    select tMatch.name
                    from DiaryEntry d2 join d2.tags tMatch
                    where d2.id = d.id
                      and :hasTags = true
                      and lower(tMatch.name) in (:tagNames)
                      and tMatch.id = (
                          select min(t2.id)
                          from DiaryEntry d3 join d3.tags t2
                          where d3.id = d.id
                            and lower(t2.name) in (:tagNames)
                      )
                ),
                (
                    select t.name
                    from DiaryEntry d4 join d4.tags t
                    where d4.id = d.id
                      and t.id = (
                          select min(t3.id)
                          from DiaryEntry d5 join d5.tags t3
                          where d5.id = d.id
                      )
                )
            )
        )
        from DiaryEntry d
        where d.user.id = :userId
          and d.status <> com.example.activity_diary.entity.enums.EntryStatus.DELETED
    
          and (
            :uiStatus is null
            or (case
                  when :now < d.whenStarted then 'PLANNED'
                  when :now <= d.whenEnded then 'ACTIVE'
                  when d.status = com.example.activity_diary.entity.enums.EntryStatus.FINISHED then 'FINISHED'
                  else 'FAILED'
                end) = :uiStatus
          )
    
          and d.whenStarted >= coalesce(:from, d.whenStarted)
          and d.whenStarted <= coalesce(:to,   d.whenStarted)
    
          and (
            :hasTags = false
            or (
              select count(distinct tt.id)
              from DiaryEntry d6 join d6.tags tt
              where d6.id = d.id
                and lower(tt.name) in (:tagNames)
            ) = :tagCount
          )
    """)
    Slice<DiaryEntryViewDto> findListByUserIdFilteredAndTags(
            @Param("userId") Long userId,
            @Param("uiStatus") String uiStatus,
            @Param("now") Instant now,

            @Param("hasTags") boolean hasTags,
            @Param("tagNames") List<String> tagNames,
            @Param("tagCount") Integer tagCount,

            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );



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
