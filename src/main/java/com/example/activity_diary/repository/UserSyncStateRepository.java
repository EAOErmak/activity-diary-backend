package com.example.activity_diary.repository;

import com.example.activity_diary.entity.UserSyncState;
import com.example.activity_diary.entity.UserSyncStateId;
import com.example.activity_diary.entity.enums.SyncEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSyncStateRepository extends JpaRepository<UserSyncState, UserSyncStateId> {

    @Modifying
    @Query("""
       UPDATE UserSyncState s
       SET s.version = s.version + 1,
           s.lastUpdated = CURRENT_TIMESTAMP
       WHERE s.userId = :userId AND s.entityType = :type
    """)
    void increment(@Param("userId") Long userId,
                   @Param("type") SyncEntityType type);

    List<UserSyncState> findAllByUserId(Long userId);
}

