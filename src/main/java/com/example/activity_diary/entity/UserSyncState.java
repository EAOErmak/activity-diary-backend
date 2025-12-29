package com.example.activity_diary.entity;

import com.example.activity_diary.entity.enums.UserSyncEntityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_sync_state",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "entity_type"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserSyncStateId.class)
public class UserSyncState {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private UserSyncEntityType entityType;

    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;
}
