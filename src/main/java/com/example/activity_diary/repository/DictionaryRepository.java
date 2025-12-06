// src/main/java/com/example/activity_diary/repository/DictionaryRepository.java
package com.example.activity_diary.repository;

import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DictionaryRepository extends JpaRepository<DictionaryItem, Long> {

    // =========================
    // ✅ УНИКАЛЬНОСТЬ
    // =========================

    boolean existsByTypeAndLabelIgnoreCase(
            DictionaryType type,
            String label
    );

    boolean existsByTypeAndLabelIgnoreCaseAndIdNot(
            DictionaryType type,
            String label,
            Long id
    );

    // =========================
    // ✅ ПОЛУЧЕНИЕ ПО ТИПУ
    // =========================

    List<DictionaryItem> findAllByTypeAndActiveTrueOrderByLabelAsc(
            DictionaryType type
    );

    List<DictionaryItem> findAllByTypeAndParentIdAndActiveTrueOrderByLabelAsc(
            DictionaryType type,
            Long parentId
    );

    List<DictionaryItem> findAllByTypeOrderByLabelAsc(
            DictionaryType type
    );

    // =========================
    // ✅ ПОИСК
    // =========================

    List<DictionaryItem> findAllByLabelContainingIgnoreCaseAndActiveTrueOrderByTypeAscLabelAsc(
            String label
    );

    List<DictionaryItem> findAllByLabelContainingIgnoreCaseOrderByTypeAscLabelAsc(
            String label
    );
}
