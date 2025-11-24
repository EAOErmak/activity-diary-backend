package com.example.activity_diary.repository;

import com.example.activity_diary.entity.dict.WhatDict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WhatDictRepository extends JpaRepository<WhatDict, Long> {
    boolean existsByNameIgnoreCaseAndWhatHappenedId(String name, Long whatHappenedId);
    List<WhatDict> findByWhatHappenedId(Long parentId);
    List<WhatDict> findByNameContainingIgnoreCase(String q);
}
