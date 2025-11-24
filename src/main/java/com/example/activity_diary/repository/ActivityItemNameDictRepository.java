package com.example.activity_diary.repository;

import com.example.activity_diary.entity.dict.ActivityItemNameDict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityItemNameDictRepository extends JpaRepository<ActivityItemNameDict, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<ActivityItemNameDict> findByNameContainingIgnoreCase(String q);
}
