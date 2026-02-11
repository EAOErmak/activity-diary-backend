package com.example.activity_diary.repository.diary;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.activity_diary.entity.diary.EntryFieldConfig;

public interface EntryFieldConfigRepository extends JpaRepository<EntryFieldConfig, Long> {
    boolean existsByNameIgnoreCase(String name);

}
