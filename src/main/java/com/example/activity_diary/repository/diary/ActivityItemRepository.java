package com.example.activity_diary.repository.diary;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.activity_diary.entity.diary.EntryMetric;

import java.util.List;

public interface ActivityItemRepository extends JpaRepository<EntryMetric, Long> {
    List<EntryMetric> findAllByDiaryEntryIdIn(List<Long> diaryIds);
}
