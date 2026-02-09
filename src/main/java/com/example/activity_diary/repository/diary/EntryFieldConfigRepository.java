package com.example.activity_diary.repository.diary;

import com.example.activity_diary.entity.EntryFieldConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryFieldConfigRepository extends JpaRepository<EntryFieldConfig, Long> {
    boolean existsByNameIgnoreCase(String name);

}
