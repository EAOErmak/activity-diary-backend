package com.example.activity_diary.repository;

import com.example.activity_diary.entity.dict.ActivityItemUnitDict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityItemUnitDictRepository extends JpaRepository<ActivityItemUnitDict, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<ActivityItemUnitDict> findByNameContainingIgnoreCase(String q);
}
