package com.example.activity_diary.repository;

import com.example.activity_diary.entity.dict.WhatHappenedDict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WhatHappenedDictRepository extends JpaRepository<WhatHappenedDict, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<WhatHappenedDict> findByNameContainingIgnoreCase(String q);
}
