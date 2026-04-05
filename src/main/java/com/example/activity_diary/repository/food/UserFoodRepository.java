package com.example.activity_diary.repository.food;

import com.example.activity_diary.entity.food.UserFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserFoodRepository extends JpaRepository<UserFood, Long> {

    boolean existsByUserIdAndDictionaryItemId(Long userId, Long dictionaryItemId);

    boolean existsByUserIdAndDictionaryItemIdAndIdNot(Long userId, Long dictionaryItemId, Long id);

    Optional<UserFood> findByIdAndUserId(Long id, Long userId);

    @Query("""
        SELECT uf
        FROM UserFood uf
        JOIN uf.dictionaryItem di
        WHERE uf.user.id = :userId
        ORDER BY di.label ASC, uf.id ASC
    """)
    List<UserFood> findAllByUserIdOrdered(Long userId);

    @Query("""
        SELECT uf
        FROM UserFood uf
        JOIN uf.dictionaryItem di
        WHERE uf.user.id = :userId
          AND lower(di.label) LIKE lower(concat('%', :query, '%'))
        ORDER BY di.label ASC, uf.id ASC
    """)
    List<UserFood> searchByUserIdAndDictionaryLabel(Long userId, String query);

    @Query("""
        SELECT uf
        FROM UserFood uf
        JOIN FETCH uf.dictionaryItem di
        WHERE uf.user.id = :userId
          AND di.id IN :dictionaryItemIds
    """)
    List<UserFood> findAllByUserIdAndDictionaryItemIdIn(
            @Param("userId") Long userId,
            @Param("dictionaryItemIds") Collection<Long> dictionaryItemIds
    );
}
