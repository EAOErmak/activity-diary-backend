package com.example.activity_diary.entity;

import com.example.activity_diary.entity.enums.SyncEntityType;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSyncStateId implements Serializable {
    private Long userId;
    private SyncEntityType entityType;
}

