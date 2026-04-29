package com.example.activity_diary.repository.food;

import com.example.activity_diary.entity.food.GeneralFood;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GeneralFoodRepository extends JpaRepository<GeneralFood, Long> {

    boolean existsByDictionaryItemId(Long dictionaryItemId);

    boolean existsByDictionaryItemIdAndIdNot(Long dictionaryItemId, Long id);

    @Override
    @EntityGraph(attributePaths = {"dictionaryItem"})
    Optional<GeneralFood> findById(Long id);

    @Query("""
        SELECT gf
        FROM GeneralFood gf
        JOIN FETCH gf.dictionaryItem di
        ORDER BY di.label ASC, gf.id ASC
    """)
    List<GeneralFood> findAllOrdered();

    @Query("""
        SELECT gf
        FROM GeneralFood gf
        JOIN FETCH gf.dictionaryItem di
        WHERE lower(di.label) LIKE lower(concat('%', :query, '%'))
        ORDER BY di.label ASC, gf.id ASC
    """)
    List<GeneralFood> searchByDictionaryLabel(String query);

    @Query("""
        SELECT gf
        FROM GeneralFood gf
        JOIN FETCH gf.dictionaryItem di
        WHERE di.id IN :dictionaryItemIds
    """)
    List<GeneralFood> findAllByDictionaryItemIdIn(@Param("dictionaryItemIds") Collection<Long> dictionaryItemIds);
}
