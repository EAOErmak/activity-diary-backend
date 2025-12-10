package com.example.activity_diary.repository;

import com.example.activity_diary.entity.EntryMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityItemRepository extends JpaRepository<EntryMetric, Long> {
    List<EntryMetric> findAllByDiaryEntryIdIn(List<Long> diaryIds);
}
