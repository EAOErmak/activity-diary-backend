package com.example.activity_diary.repository;

import com.example.activity_diary.entity.GlobalSyncState;
import com.example.activity_diary.entity.enums.GlobalSyncEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface GlobalSyncStateRepository
        extends JpaRepository<GlobalSyncState, GlobalSyncEntityType> {

    @Modifying
    @Query("""
        update GlobalSyncState g
        set g.version = g.version + 1,
            g.lastUpdated = CURRENT_TIMESTAMP
        where g.entityType = :type
    """)
    int increment(GlobalSyncEntityType type);
}

