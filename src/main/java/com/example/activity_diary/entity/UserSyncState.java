package com.example.activity_diary.entity;

import com.example.activity_diary.entity.enums.SyncEntityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sync_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserSyncStateId.class)
public class UserSyncState {

    @Id
    private Long userId;

    @Id
    @Enumerated(EnumType.STRING)
    private SyncEntityType entityType;

    private Long version;

    private LocalDateTime lastUpdated;
}

