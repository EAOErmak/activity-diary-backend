// src/main/java/com/example/activity_diary/repository/DictionaryRepository.java
package com.example.activity_diary.repository;

import com.example.activity_diary.entity.dict.DictionaryItem;
import com.example.activity_diary.entity.enums.DictionaryType;
import com.example.activity_diary.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DictionaryRepository extends JpaRepository<DictionaryItem, Long> {

    boolean existsByTypeAndLabelIgnoreCase(
            DictionaryType type,
            String label
    );

    boolean existsByTypeAndLabelIgnoreCaseAndIdNot(
            DictionaryType type,
            String label,
            Long id
    );

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

    List<DictionaryItem> findAllByLabelContainingIgnoreCaseAndActiveTrueOrderByTypeAscLabelAsc(
            String label
    );

    List<DictionaryItem> findAllByLabelContainingIgnoreCaseOrderByTypeAscLabelAsc(
            String label
    );

    @Query("""
        SELECT d FROM DictionaryItem d
        WHERE d.type = :type
        AND d.active = true
        AND (d.allowedRole IS NULL OR d.allowedRole = :role)
        AND (:parentId IS NULL OR d.parent.id = :parentId)
        ORDER BY d.label ASC
    """)
    List<DictionaryItem> findByTypeAndVisibleForUser(
            DictionaryType type,
            Long parentId,
            Role role
    );

    @Query("""
        SELECT d FROM DictionaryItem d
        WHERE d.allowedRole IS NULL OR d.allowedRole = :role
        ORDER BY d.label ASC
    """)
    List<DictionaryItem> findAllVisibleForUser(
            Role role
    );

    @Query("""
        SELECT d
        FROM DictionaryItem d
        WHERE 
            d.active = true
            AND lower(d.label) LIKE lower(concat('%', :query, '%'))
            AND (
                d.allowedRole IS NULL
                OR :roleName = 'ADMIN'
                OR d.allowedRole = :roleName
            )
        ORDER BY d.type ASC, d.label ASC
    """)
    List<DictionaryItem> searchVisibleForUser(
            @org.springframework.data.repository.query.Param("query") String query,
            @org.springframework.data.repository.query.Param("roleName") String roleName
    );

    boolean existsByLabel(String label);
}
