package com.example.activity_diary.entity;

import com.example.activity_diary.entity.enums.GlobalSyncEntityType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "global_sync_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSyncState {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private GlobalSyncEntityType entityType;

    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;
}
