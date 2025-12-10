package com.example.activity_diary.entity;

import com.example.activity_diary.entity.enums.SyncEntityType;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserSyncStateId implements Serializable {

    private Long userId;
    private SyncEntityType entityType;
}
