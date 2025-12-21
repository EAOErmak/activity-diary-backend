package com.example.activity_diary.repository;

import com.example.activity_diary.entity.UserSyncState;
import com.example.activity_diary.entity.UserSyncStateId;
import com.example.activity_diary.entity.enums.UserSyncEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserSyncStateRepository extends JpaRepository<UserSyncState, UserSyncStateId> {

    @Modifying
    @Query("""
       UPDATE UserSyncState s
       SET s.version = s.version + 1,
           s.lastUpdated = CURRENT_TIMESTAMP
       WHERE s.userId = :userId
         AND s.entityType = :type
    """)
    int increment(
            @Param("userId") Long userId,
            @Param("type") UserSyncEntityType type
    );

    List<UserSyncState> findAllByUserId(Long userId);

    Optional<UserSyncState> findByUserIdAndEntityType(Long userId, UserSyncEntityType type);
}
