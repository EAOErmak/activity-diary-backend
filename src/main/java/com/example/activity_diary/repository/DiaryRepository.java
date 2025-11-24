package com.example.activity_diary.repository;

import com.example.activity_diary.entity.DiaryEntry;
import com.example.activity_diary.entity.enums.EntryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
