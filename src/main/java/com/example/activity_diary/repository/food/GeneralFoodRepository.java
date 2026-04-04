package com.example.activity_diary.repository.food;

import com.example.activity_diary.entity.food.GeneralFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GeneralFoodRepository extends JpaRepository<GeneralFood, Long> {

    boolean existsByDictionaryItemId(Long dictionaryItemId);

    boolean existsByDictionaryItemIdAndIdNot(Long dictionaryItemId, Long id);

    @Query("""
        SELECT gf
        FROM GeneralFood gf
        JOIN gf.dictionaryItem di
        ORDER BY di.label ASC, gf.id ASC
    """)
    List<GeneralFood> findAllOrdered();

    @Query("""
        SELECT gf
        FROM GeneralFood gf
        JOIN gf.dictionaryItem di
        WHERE lower(di.label) LIKE lower(concat('%', :query, '%'))
        ORDER BY di.label ASC, gf.id ASC
    """)
    List<GeneralFood> searchByDictionaryLabel(String query);
}
