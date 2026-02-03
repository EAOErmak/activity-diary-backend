package com.example.activity_diary.entity;

import com.example.activity_diary.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(
        name = "tag_suggestion",
        indexes = {
                @Index(name = "idx_tag_suggestion_last_seen", columnList = "last_seen_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_tag_suggestion_name", columnNames = {"tag_name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TagSuggestion extends BaseEntity {

    @Column(name = "tag_name", nullable = false, length = 64)
    private String tagName; // нормализованное имя

    @Column(name = "user_count", nullable = false)
    @Builder.Default
    private int userCount = 0;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    public void markSeen(Instant now) {
        this.lastSeenAt = now == null ? Instant.now() : now;
    }

    public void incrementUserCount() {
        this.userCount += 1;
    }
}
