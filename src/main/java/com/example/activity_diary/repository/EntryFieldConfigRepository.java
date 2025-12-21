package com.example.activity_diary.repository;

import com.example.activity_diary.entity.EntryFieldConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EntryFieldConfigRepository extends JpaRepository<EntryFieldConfig, Long> {
    boolean existsByNameIgnoreCase(String name);

}
